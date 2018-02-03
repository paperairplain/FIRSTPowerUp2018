package team6458.cmd;

import team6458.SemiRobot;
import team6458.util.Utils;

/**
 * A command that attempts to rotate the robot left or right in a given direction.
 */
public class RotateCommand extends RobotCommand {

    /**
     * The default angle tolerance in degrees at which the target angle and real angle have to match by.
     */
    public static final double ANGLE_TOLERANCE = 4.0;
    /**
     * The default speed gradient to use.
     * <p>
     * Max of 0.4, min of 0.2, range of 25 deg starting at 10 deg.
     */
    public static final SpeedGradient DEFAULT_GRADIENT = new SpeedGradient(0.4, 0.2, 25.0, 10.0);

    public final double headingChange;
    public final SpeedGradient speedGradient;

    private double originalOrientation;
    private double targetOrientation;

    /**
     * Constructor.
     *
     * @param robot         The robot instance
     * @param headingChange The amount to change the heading by, positive is clockwise
     * @param gradient      The speed gradient to use
     */
    public RotateCommand(SemiRobot robot, double headingChange, SpeedGradient gradient) {
        super(robot);
        requires(robot.getDrivetrain());
        setTimeout(8.0);

        this.headingChange = headingChange;
        this.speedGradient = gradient;
    }

    public RotateCommand(SemiRobot robot, double headingChange) {
        this(robot, headingChange, DEFAULT_GRADIENT);
    }

    @Override
    public synchronized void start() {
        super.start();
        originalOrientation = robot.getSensors().gyro.getAngle();
        targetOrientation = originalOrientation + headingChange;
    }

    @Override
    protected void execute() {
        super.execute();
        robot.getDrivetrain().drive.curvatureDrive(0.0,
                Math.copySign(getCurrentThrottle(), targetOrientation - originalOrientation), true);
    }

    @Override
    protected void end() {
        super.end();
        robot.getDrivetrain().drive.curvatureDrive(0.0, 0.0, true);
    }

    @Override
    public synchronized boolean isInterruptible() {
        return true;
    }

    /**
     * @return True if the current heading has overshot the target, false otherwise
     */
    public final boolean hasOvershot() {
        double currentAngle = robot.getSensors().gyro.getAngle();
        return (headingChange >= 0.0 ? currentAngle > targetOrientation : currentAngle < targetOrientation);
    }

    /**
     * @return The current throttle to use based on the selected speed gradient
     */
    public final double getCurrentThrottle() {
        if (hasOvershot())
            return 0.0;

        double currentAngle = robot.getSensors().gyro.getAngle();
        double remainingAngle = Math.abs(currentAngle - targetOrientation);

        if (remainingAngle <= speedGradient.rangeOffset) {
            return speedGradient.minSpeed;
        } else if (remainingAngle >= speedGradient.rangeOffset + speedGradient.degreesRange) {
            return speedGradient.maxSpeed;
        } else {
            // Compute the throttle which should be in between min and max speed
            double rangePercentage = (remainingAngle - speedGradient.rangeOffset) / speedGradient.degreesRange;
            return speedGradient.minSpeed + (speedGradient.maxSpeed - speedGradient.minSpeed) * rangePercentage;
        }
    }

    @Override
    protected boolean isFinished() {
        return Utils.isEqual(robot.getSensors().gyro.getAngle(),
                targetOrientation, ANGLE_TOLERANCE) || hasOvershot() || isTimedOut();
    }

    /**
     * A simple value holder class that holds the speed curve/gradient for rotation.
     */
    public static final class SpeedGradient {

        /**
         * The maximum rotation speed between 0.0 and 1.0. If this is too fast, the gyroscope will not keep up.
         */
        public final double maxSpeed;
        /**
         * The minimum rotation speed between 0.0 and 1.0. If this is too low, the wheels may not move.
         */
        public final double minSpeed;
        /**
         * The range in degrees where the {@link #maxSpeed} transitions to the {@link #minSpeed} linearly.
         * This is based on the degrees remaining plus the {@link #rangeOffset}.
         */
        public final double degreesRange;
        /**
         * The start of the {@link #degreesRange}. Must greater than or equal to zero.
         * The remaining angle from zero to this value will always be at the {@link #minSpeed}.
         */
        public final double rangeOffset;

        public SpeedGradient(double maxSpeed, double minSpeed, double degreesRange, double rangeOffset) {
            if (maxSpeed < 0 || maxSpeed > 1 || minSpeed < 0 || minSpeed > maxSpeed || rangeOffset < 0) {
                throw new IllegalArgumentException("Invalid parameters passed for speed gradient");
            }

            this.maxSpeed = maxSpeed;
            this.minSpeed = minSpeed;
            this.degreesRange = degreesRange;
            this.rangeOffset = rangeOffset;
        }
    }
}
