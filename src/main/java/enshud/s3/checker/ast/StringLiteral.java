package enshud.s3.checker.ast;

import java.util.Objects;

import enshud.s3.checker.Checker;
import enshud.s3.checker.Procedure;
import enshud.s2.parser.node.basic.TokenNode;
import enshud.s3.checker.type.IType;
import enshud.s3.checker.type.RegularType;
import enshud.s3.checker.type.StringType;


public class StringLiteral implements IConstant
{
    final TokenNode str;
    IType           type;

    public StringLiteral(TokenNode str)
    {
        this.str = Objects.requireNonNull(str);
        type = new StringType(length());
    }

    @Override
    public String toString()
    {
        return str.getString();
    }

    public int length()
    {
        return toString().length();
    }

    @Override
    public IType getType()
    {
        return type;
    }

    @Override
    public int getLine()
    {
        return str.getLine();
    }

    @Override
    public int getColumn()
    {
        return str.getColumn();
    }

    @Override
    public void retype(IType new_type)
    {
        if( type instanceof StringType )
        {
            type = new_type;
        }
    }

    @Override
    public IType check(Procedure proc, Checker checker)
    {
        return type;
    }
    
    /*@Override
    public void compile(StringBuilder codebuilder, Procedure proc, LabelGenerator l_gen)
    {
        codebuilder.append(" LAD GR2,=").append(toString()).append(System.lineSeparator());
        if(type != RegularType.CHAR)
        {
            if(length() == 0)
            {
                codebuilder.append(" XOR GR1,GR1").append(System.lineSeparator());
            }
            else
            {
                codebuilder.append(" LAD GR1,").append(length()).append(System.lineSeparator());
            }
        }
    }*/
}


