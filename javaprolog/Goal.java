import pddl4j.Domain;
import pddl4j.PDDLObject;
import pddl4j.Parser;
import pddl4j.Problem;
import pddl4j.exp.AtomicFormula;
import pddl4j.exp.Exp;

/**
 * Wraps a pddl expression Exp. A goal is a logical expression (Exp) which describes the desired relationship between objects in the world.
 */
public class Goal {

    private final Exp pddlExpression;

    public Goal(Exp pddlExpression){
        this.pddlExpression = pddlExpression;
    }

	@Override
	public String toString() {
		return pddlExpression.toString();
	}

    public Exp getPddlExpression() {
        return pddlExpression;
    }
}

//TODO: Integrate properly with the library. Some random example code:
//        this.formula = new AtomicFormula(predicate);
//        formula.add();
//        PDDLObject pddl = new PDDLObject();
//        Exp goal = pddl.getGoal();

// Creates an instance of the java pddl parser
//        Parser parser = new Parser(options);
//        Domain domain = parser.parse(new File(args[0]));
//        Problem problem = parser.parse(new File(args[1]));
//        PDDLObject obj = parser.link(domain, problem);

//    private final AtomicFormula formula;
//Goal should be an atomic formula? Instead of internal represenation?
