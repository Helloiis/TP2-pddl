package src.fr.uga.pddl4j.tp2.prw;

import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.RequireKey;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Goal;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;


/**
 *
 * @author H. BERNARD
 * @version 1.0 - 02.11.2024
 */
@CommandLine.Command(name = "PRW",
    version = "PRW 1.0",
    description = "Solves a specified planning problem using Pure Random Search strategy.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")
public class PRW extends AbstractPlanner {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(PRW.class.getName());

    /**
     * Maximum number of steps before restarting the search.
     */
    private static final int MAX_STEPS = 1000;

    private Random random = new Random();
    
    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be instantiated.
     */
    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }
    /**
     * Search a solution plan to a specified domain and problem using Monte Carlo random search.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */
    @Override
    public Plan solve(Problem problem) {
        if (!this.isSupported(problem)) {
            throw new ProblemNotSupportedException("Problem not supported");
        }

        final State initialState = new State(problem.getInitialState());
        final Goal goal = problem.getGoal();
        final Plan plan = new Plan();

        State s = initialState;
        int hmin = heuristic(s, goal);  // Heuristic value for the initial state
        int counter = 0;

        while (!s.satisfies(goal)) {
            if (counter > MAX_STEPS || isDeadEnd(s)) {
                // Restart from the initial state
                s = initialState;
                counter = 0;
                plan.clear();  // Clear the current plan if restarting
            } else {
                // Perform a random walk
                s = monteCarloRandomWalk(s, plan, goal);

                // Update heuristic and counter
                int h = heuristic(s, goal);
                if (h < hmin) {
                    hmin = h;
                    counter = 0;
                } else {
                    counter++;
                }
            }
        }

        LOGGER.info("Plan found with " + plan.size() + " steps.");
        return plan;
    }

    /**
     * Heuristic function to estimate the distance to the goal.
     *
     * @param s the current state.
     * @param goal the goal to reach.
     * @return an integer representing the heuristic value.
     */
    private int heuristic(State s, Goal goal) {
        return s.calculateHeuristic(goal);
    }

    /**
     * Check if a given state is a dead end.
     *
     * @param s the state to check.
     * @return true if the state is a dead end, false otherwise.
     */
    private boolean isDeadEnd(State s) {
        return s.isDeadEnd();
    }

    /**
     * Perform a Monte Carlo random walk from the current state.
     *
     * @param s the current state.
     * @param plan the plan being constructed.
     * @param goal the goal to reach.
     * @return the new state after the random walk.
     */
    private State monteCarloRandomWalk(State s, Plan plan, Goal goal) {
        List<Action> actions = s.getPossibleActions();
        if (actions.isEmpty()) return s;

        Action randomAction = actions.get(random.nextInt(actions.size()));
        plan.addAction(randomAction);  // Add the chosen action to the plan
        return randomAction.apply(s);  // Apply the action to get the new state
    }

    /**
     * The main method of the <code>ASP</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        try {
            final PRW planner = new PRW();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute(args);
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }
    @Override
    public boolean isSupported(Problem problem) {
        return (problem.getRequirements().contains(RequireKey.ACTION_COSTS)
            || problem.getRequirements().contains(RequireKey.CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.CONTINOUS_EFFECTS)
            || problem.getRequirements().contains(RequireKey.DERIVED_PREDICATES)
            || problem.getRequirements().contains(RequireKey.DURATIVE_ACTIONS)
            || problem.getRequirements().contains(RequireKey.DURATION_INEQUALITIES)
            || problem.getRequirements().contains(RequireKey.FLUENTS)
            || problem.getRequirements().contains(RequireKey.GOAL_UTILITIES)
            || problem.getRequirements().contains(RequireKey.METHOD_CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.NUMERIC_FLUENTS)
            || problem.getRequirements().contains(RequireKey.OBJECT_FLUENTS)
            || problem.getRequirements().contains(RequireKey.PREFERENCES)
            || problem.getRequirements().contains(RequireKey.TIMED_INITIAL_LITERALS)
            || problem.getRequirements().contains(RequireKey.HIERARCHY))
            ? false : true;
    }
}


/*
 *  Classes 
 */

class State {

    public int calculateHeuristic(Goal goal) {
        return 0; 
    }

    public boolean isDeadEnd() {
        return false;
    }

    public boolean satisfies(Goal goal) {
        return false; 
    }

    public List<Action> getPossibleActions() {
        return List.of();
    }
}

class Action {
    public State apply(State s) {
        return s;
    }
}

class Goal {
}
