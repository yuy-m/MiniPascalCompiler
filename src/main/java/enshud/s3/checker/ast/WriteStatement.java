package enshud.s3.checker.ast;

import java.util.List;
import java.util.Objects;

import enshud.s3.checker.Checker;
import enshud.s3.checker.Procedure;
import enshud.s3.checker.type.IType;
import enshud.s3.checker.type.StringType;
import enshud.s4.compiler.LabelGenerator;
import enshud.s3.checker.type.RegularType;


public class WriteStatement implements IReadWriteStatement
{
    final ExpressionList exps;

    public WriteStatement(ExpressionList exps)
    {
        this.exps = Objects.requireNonNull(exps);
    }

    public List<IExpression> getExpressions()
    {
        return exps.getList();
    }

    @Override
    public IType check(Procedure proc, Checker checker)
    {
        int i = 1;
        for(final IExpression exp: getExpressions())
        {
            IType type = exp.check(proc, checker);

            if( type instanceof StringType )
            {
            	if(((StringType)type).getSize() == 1)
            	{
            		type = RegularType.CHAR;
            	}
                exp.retype(type);
            }
            else if( type != RegularType.INTEGER && type != RegularType.CHAR && !type.isArrayOf(RegularType.CHAR) )
            {
                checker.addErrorMessage(
                    proc, exp, "incompatible type: " + Checker.getOrderString(i)
                            + " argument of writeln must be INTEGER, CHAR, or array of CHAR, but is " + type + "."
                );
            }
            else if( type.isUnknown() )
            {
                checker.addErrorMessage(
                    proc, exp, "cannot identify the type of " + Checker.getOrderString(i) + " argument of writeln."
                );
            }
            ++i;
        }
        return null;
    }

    @Override
    public void compile(StringBuilder codebuilder, Procedure proc, LabelGenerator l_gen)
    {
        for(IExpression e: getExpressions())
        {
            e.compile(codebuilder, proc, l_gen);

            if( e.getType() == RegularType.CHAR )
            {
                codebuilder.append(" CALL WRTCH").append(System.lineSeparator());
            }
            else if( e.getType() == RegularType.INTEGER )
            {
                codebuilder.append(" CALL WRTINT").append(System.lineSeparator());
            }
            else if( e.getType().isArrayOf(RegularType.CHAR) )
            {
                codebuilder.append(" CALL WRTSTR").append(System.lineSeparator());
            }
            else
            {
                assert false: "type error";
            }
        }
        codebuilder.append(" CALL WRTLN").append(System.lineSeparator());
    }

    @Override
    public String toString()
    {
        return "";
    }

    @Override
    public int getLine()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumn()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printBodyln(String indent)
    {
        exps.println(indent + "  ");
    }
}

