// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static frc.robot.Constants.VisionConstants.FRONT_CAMERA_CONFIG;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.OperatorConstants;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  private final Drive drive = new Drive();
  private final Vision vision = new Vision(FRONT_CAMERA_CONFIG);
  private final Field2d field = new Field2d();
  // private final DifferentialDrivePoseEstimator estimator = new DifferentialDrivePoseEstimator(null, null, kDefaultPeriod, kDefaultPeriod, null)

  private final CommandXboxController driver = new CommandXboxController(OperatorConstants.DRIVER_CONTROLLER);

  public Robot() {
    SmartDashboard.putData(field);
    addPeriodic(() -> CommandScheduler.getInstance().run(), kDefaultPeriod);
    addPeriodic(() -> {
      var poses = vision.getEstimatedGlobalPoses();
      if (poses.length > 0) field.setRobotPose(poses[0].estimatedPose().estimatedPose.toPose2d());
    }, kDefaultPeriod);
    drive.setDefaultCommand(drive.drive(() -> -driver.getLeftY(), () -> -driver.getRightX()));
  }

}
