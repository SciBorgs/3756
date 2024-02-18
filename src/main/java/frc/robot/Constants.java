// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import frc.robot.Vision.CameraConfig;


/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static class OperatorConstants {
    public static final int DRIVER_CONTROLLER = 0;
  }

  public static class DriveConstants {
    public static final int FRONT_LEFT = 0;
    public static final int BACK_LEFT = 1;
    public static final int FRONT_RIGHT = 2;
    public static final int BACK_RIGHT = 3;
  
    public static final TalonSRXConfiguration CONFIG = new TalonSRXConfiguration();
    {
      CONFIG.peakCurrentLimit = 40;
      CONFIG.peakCurrentDuration = 1500;
      CONFIG.continuousCurrentLimit = 30;
      CONFIG.enableOptimizations = true;
    }
  }

  public static class VisionConstants {
    public static final AprilTagFieldLayout TAG_LAYOUT =
      AprilTagFields.kDefaultField.loadAprilTagLayoutField();

    public static final CameraConfig FRONT_CAMERA_CONFIG =
        new CameraConfig(
            "cam", new Transform3d(new Translation3d(0.5, 0.5, -0.5), new Rotation3d(0, 0, 0)));

    // OV9281 constants for our configuration
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final Rotation2d FOV = Rotation2d.fromDegrees(100);

    public static final Matrix<N3, N1> SINGLE_TAG_STD_DEVS = VecBuilder.fill(4, 4, 8);
    public static final Matrix<N3, N1> MULTIPLE_TAG_STD_DEVS = VecBuilder.fill(0.9, 0.9, 4);
  }
}
