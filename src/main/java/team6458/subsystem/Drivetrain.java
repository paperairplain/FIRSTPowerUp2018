package team6458.subsystem;

import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import team6458.SemiRobot;
import team6458.util.PWMPorts;
import team6458.util.Registrar;

/**
 * The drivetrain subsystem. This subsystem controls all vehicular aspects of the robot,
 * specifically the motors that make it move forwards/backwards/turn.
 */
public class Drivetrain extends RobotSubsystem {

    private final Spark leftMotor = Registrar.spark(PWMPorts.LEFT_MOTOR);
    private final Spark rightMotor = Registrar.spark(PWMPorts.RIGHT_MOTOR);
    public final DifferentialDrive drive = new DifferentialDrive(leftMotor, rightMotor);

    public Drivetrain(SemiRobot robot) {
        super(robot, "Drivetrain");
    }

    @Override
    protected void initDefaultCommand() {

    }
}