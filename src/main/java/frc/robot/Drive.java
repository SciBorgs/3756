package frc.robot;

import static frc.robot.Constants.DriveConstants.*;

import java.util.List;
import java.util.function.DoubleSupplier;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;


public class Drive extends SubsystemBase {

    // TalonSRXConfiguration c = new TalonSRXConfiguration().
    private final WPI_TalonSRX frontLeft = new WPI_TalonSRX(FRONT_LEFT);
    private final WPI_TalonSRX backLeft = new WPI_TalonSRX(BACK_LEFT);
    private final WPI_TalonSRX frontRight = new WPI_TalonSRX(FRONT_RIGHT);
    private final WPI_TalonSRX backRight = new WPI_TalonSRX(BACK_RIGHT);
    private final DifferentialDrive drive = new DifferentialDrive(frontLeft::set, frontRight::set);

    public Drive() {
        for (var motor : List.of(frontLeft, backLeft, frontRight, backRight)) {
            motor.configAllSettings(CONFIG);
            motor.setNeutralMode(NeutralMode.Brake);
        }
        
        backLeft.follow(frontLeft);
        backRight.follow(frontRight);
    }

    public Command drive(DoubleSupplier speed, DoubleSupplier rotation) {
        return run(() -> drive.arcadeDrive(speed.getAsDouble(), rotation.getAsDouble())).withName("joystick drive");
    }
}
