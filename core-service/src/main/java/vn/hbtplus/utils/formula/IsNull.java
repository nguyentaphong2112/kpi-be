package vn.hbtplus.utils.formula;

import java.util.Stack;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.ParserVisitor;
import org.nfunk.jep.Variable;
import org.nfunk.jep.function.PostfixMathCommand;
import org.nfunk.jep.function.SpecialEvaluationI;

public class IsNull extends PostfixMathCommand implements SpecialEvaluationI {

    private static final Double ZERO = (double) 0;
    private static final Double ONE = (double) 1;

    /**
     *
     */
    public IsNull() {
        super();
        numberOfParameters = 1;
    }

    /**
     * Based this code on the a solution from pfafrich at
     * http://www.singularsys.com/support/forum.html
     */
    @Override
    public Object evaluate(Node node, Object data, ParserVisitor pv, Stack stack) throws ParseException {
        if (node.jjtGetNumChildren() != 1) {
            throw new ParseException("bad number of parameters");
        }

        Node child = node.jjtGetChild(0);
        if (child instanceof ASTVarNode) {
            Variable var = ((ASTVarNode) child).getVar();
            if (var == null) {
                stack.push(ONE);
                return ONE;
            }
            Object val = var.getValue();
            if (val == null) {
                stack.push(ONE);
                return ONE;
            }
            stack.push(ZERO);
            return ZERO;
        } else {
            Object val = null;
            try {
                child.jjtAccept(pv, data);
                val = stack.pop();
            } catch (ParseException pe) {
                // we will receive this exception if the argument to isNull is
                // an expression which contains at least 1 null variable.
                stack.clear();
            } catch (Throwable th) {
                th.printStackTrace();
            }

            if (val == null) {
                stack.push(ONE);
                return ONE;
            }
            stack.push(ZERO);
            return ZERO;
        }
    }

}


