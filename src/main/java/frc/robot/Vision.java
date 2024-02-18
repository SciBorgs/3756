package frc.robot;

import static frc.robot.Constants.VisionConstants.FOV;
import static frc.robot.Constants.VisionConstants.HEIGHT;
import static frc.robot.Constants.VisionConstants.MULTIPLE_TAG_STD_DEVS;
import static frc.robot.Constants.VisionConstants.SINGLE_TAG_STD_DEVS;
import static frc.robot.Constants.VisionConstants.TAG_LAYOUT;
import static frc.robot.Constants.VisionConstants.WIDTH;

import java.util.ArrayList;
import java.util.List;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonPipelineResult;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import frc.robot.Constants.VisionConstants;

public class Vision {
  public static record CameraConfig(String name, Transform3d robotToCam) {}

  public static record PoseEstimate(EstimatedRobotPose estimatedPose, Matrix<N3, N1> standardDev) {}

  private final PhotonCamera[] cameras;
  private final PhotonPoseEstimator[] estimators;
  private final PhotonCameraSim[] simCameras;

  private VisionSystemSim visionSim;

  public Vision(CameraConfig... configs) {
    cameras = new PhotonCamera[configs.length];
    estimators = new PhotonPoseEstimator[configs.length];
    simCameras = new PhotonCameraSim[configs.length];

    for (int i = 0; i < configs.length; i++) {
      PhotonCamera camera = new PhotonCamera(configs[i].name());
      PhotonPoseEstimator estimator =
          new PhotonPoseEstimator(
              VisionConstants.TAG_LAYOUT,
              PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
              camera,
              configs[i].robotToCam());

      estimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);

      cameras[i] = camera;
      estimators[i] = estimator;
    }

    if (Robot.isSimulation()) {
      visionSim = new VisionSystemSim("main");
      visionSim.addAprilTags(VisionConstants.TAG_LAYOUT);

      for (int i = 0; i < cameras.length; i++) {
        var prop = new SimCameraProperties();
        prop.setCalibration(WIDTH, HEIGHT, FOV);
        prop.setCalibError(0.35, 0.10);
        prop.setFPS(45);
        prop.setAvgLatencyMs(12);
        prop.setLatencyStdDevMs(3.5);

        PhotonCameraSim cameraSim = new PhotonCameraSim(cameras[i], prop);
        cameraSim.setMaxSightRange(7);
        cameraSim.enableRawStream(true);
        cameraSim.enableProcessedStream(true);
        cameraSim.enableDrawWireframe(true);

        visionSim.addCamera(cameraSim, configs[i].robotToCam());
        simCameras[i] = cameraSim;
      }
    }
  }

  /**
   * Returns a list of all currently visible pose estimates and their standard deviation vectors.
   *
   * @return An {@link EstimatedRobotPose} with an estimated pose, estimate timestamp, and targets
   *     used for estimation.
   */
  public PoseEstimate[] getEstimatedGlobalPoses() {
    List<PoseEstimate> estimates = new ArrayList<>();
    for (int i = 0; i < estimators.length; i++) {
      var result = cameras[i].getLatestResult();
      var estimate = estimators[i].update(result);
      estimate.ifPresent(
          e ->
              estimates.add(
                  new PoseEstimate(e, getEstimationStdDevs(e.estimatedPose.toPose2d(), result))));
    }
    return estimates.toArray(PoseEstimate[]::new);
  }

  /**
   * The standard deviations of the estimated pose from {@link #getEstimatedGlobalPose()}, for use
   * with {@link edu.wpi.first.math.estimator.SwerveDrivePoseEstimator SwerveDrivePoseEstimator}.
   * This should only be used when there are targets visible.
   *
   * @param estimatedPose The estimated pose to guess standard deviations for.
   */
  public Matrix<N3, N1> getEstimationStdDevs(
      Pose2d estimatedPose, PhotonPipelineResult pipelineResult) {
    var estStdDevs = SINGLE_TAG_STD_DEVS;
    var targets = pipelineResult.getTargets();
    int numTags = 0;
    double avgDist = 0;
    for (var tgt : targets) {
      var tagPose = TAG_LAYOUT.getTagPose(tgt.getFiducialId());
      if (tagPose.isEmpty()) continue;
      numTags++;
      avgDist +=
          tagPose.get().toPose2d().getTranslation().getDistance(estimatedPose.getTranslation());
    }
    if (numTags == 0) return estStdDevs;
    avgDist /= numTags;
    // Decrease std devs if multiple targets are visibleX
    if (numTags > 1) estStdDevs = MULTIPLE_TAG_STD_DEVS;
    // Increase std devs based on (average) distance
    if (numTags == 1 && avgDist > 4)
      estStdDevs = VecBuilder.fill(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    else estStdDevs = estStdDevs.times(1 + (avgDist * avgDist / 30));

    return estStdDevs;
  }

  /**
   * Updates the vision field simulation. This method should not be called when code is running on
   * the robot.
   */
  public void simulationPeriodic(Pose2d robotSimPose) {
    visionSim.update(robotSimPose);
  }
}
