package main;
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

    public String getPddlExpressionStr() {
        return pddlExpressionStr;
    }

    private String pddlExpressionStr;
    private Exp pddlExpression;

    public Goal(Exp pddlExpression){   //TODO: use this constructor instead
        this.pddlExpression = pddlExpression;
    }

    public Goal(String pddlExpression){
        this.pddlExpressionStr = pddlExpression;
    }

	@Override
	public String toString() {
		return pddlExpressionStr;//TODO pddlExpression.toString();
	}



    public Exp getPddlExpression() {
        return pddlExpression;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Goal goal = (Goal) o;

        if (pddlExpression != null ? !pddlExpression.equals(goal.pddlExpression) : goal.pddlExpression != null)
            return false;
        if (pddlExpressionStr != null ? !pddlExpressionStr.equals(goal.pddlExpressionStr) : goal.pddlExpressionStr != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pddlExpressionStr != null ? pddlExpressionStr.hashCode() : 0;
        result = 31 * result + (pddlExpression != null ? pddlExpression.hashCode() : 0);
        return result;
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
