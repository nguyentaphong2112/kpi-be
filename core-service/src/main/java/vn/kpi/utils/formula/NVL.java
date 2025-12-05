package vn.kpi.utils.formula;

import org.nfunk.jep.ParseException;
import java.util.Stack;
import org.nfunk.jep.function.PostfixMathCommand;

/**
 *
 * @author Admin
 */
public class NVL extends PostfixMathCommand {

    public NVL() {
        this.numberOfParameters = 2;
    }

    @Override
    public void run(Stack inStack)
            throws ParseException {
        checkStack(inStack);

        Object param2 = inStack.pop();
        Object param1 = inStack.pop();

        inStack.push(param1 == null ? param2 : param1);
    }
}

