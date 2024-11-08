package src.fr.uga.pddl4j.tp2.prw;

import fr.uga.pddl4j.heuristics.state.StateHeuristic;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.RequireKey;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Effect;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.Condition;

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
    description = "Solves a specified planning problem using Pure Random Walk Search strategy.",
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
     * The HEURISTIC property used for planner configuration.
     */
    public static final String HEURISTIC_SETTING = "HEURISTIC";

    /**
     * The default value of the HEURISTIC property used for planner configuration.
     */
    public static final StateHeuristic.Name DEFAULT_HEURISTIC = StateHeuristic.Name.FAST_FORWARD;

    /**
     * The WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final String WEIGHT_HEURISTIC_SETTING = "WEIGHT_HEURISTIC";

    /**
     * The default value of the WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final double DEFAULT_WEIGHT_HEURISTIC = 1.0;

    /**
     * The weight of the heuristic.
     */
    private double heuristicWeight;

    /**
     * The name of the heuristic used by the planner.
     */
    private StateHeuristic.Name heuristic;
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

    @CommandLine.Option(names = {"-e", "--heuristic"}, defaultValue = "FAST_FORWARD",
    description = "Set the heuristic : AJUSTED_SUM, AJUSTED_SUM2, AJUSTED_SUM2M, COMBO, "
        + "MAX, FAST_FORWARD SET_LEVEL, SUM, SUM_MUTEX (preset: FAST_FORWARD)")
    public void setHeuristic(StateHeuristic.Name heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * Returns the name of the heuristic used by the planner to solve a planning problem.
     *
     * @return the name of the heuristic used by the planner to solve a planning problem.
     */
    public final StateHeuristic.Name getHeuristic() {
        return this.heuristic;
    }

    /**
     * Returns the weight of the heuristic.
     *
     * @return the weight of the heuristic.
     */
    public final double getHeuristicWeight() {
        return this.heuristicWeight;
    }
    /**
     * Search a solution plan to a specified domain and problem using Monte Carlo random search.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
          * @throws ProblemNotSupportedException 
          */
         @Override
        public Plan solve(Problem problem) throws ProblemNotSupportedException {
        if (!this.isSupported(problem)) {
            throw new ProblemNotSupportedException("Problem not supported");
        }

        final StateHeuristic heuristic = StateHeuristic.getInstance(this.getHeuristic(), problem);
        final State initialState = new State(problem.getInitialState());
        final Condition goal = problem. getGoal();
        final Plan plan = new SequentialPlan();

        State s = initialState;
    
        int hmin = 1000000;  // Heuristic value for the initial state
        int counter = 0;

        while (!s.satisfy(goal)) {
                    if (counter > MAX_STEPS || isDeadEnd(s, problem)) { //ajout de deadEnd, pour qualifier un état d'impasse ?
                        // Restart from the initial state
                        s = initialState;
                        counter = 0;
                        plan.clear();  // Clear the current plan if restarting
                    } else {
                        // Perform a random walk
                        s = monteCarloRandomWalk(s, plan, goal, problem);
        
                        // Update heuristic and counter
                        int h = heuristic.estimate(s, goal); //heuristique à définir
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
        
            public boolean isDeadEnd(State state, Problem problem) {
                List<Action> actions = problem.getActions();
            
                boolean hasApplicableAction = actions.stream()
                    .anyMatch(action -> state.satisfy(action.getPrecondition()));
            
                return !hasApplicableAction;
            }
            
        
            /**
             * Perform a Monte Carlo random walk from the current state.
             *
             * @param s the current state.
             * @param plan the plan being constructed.
             * @param goal the goal to reach.
             * @return the new state after the random walk.
             */
            private State monteCarloRandomWalk(State s, Plan plan, Condition goal, Problem p) {
                List<Action> actions = p.getActions();
                if (actions.isEmpty()) return s;
        
                Action randomAction = actions.get(random.nextInt(actions.size()));
                plan.add(plan.size(),randomAction);  // Add the chosen action to the plan
                Effect effects = randomAction.getUnconditionalEffect();
                s.apply(effects);// Apply the action to get the new state
                return s;  // return the new state
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
        
        
       
