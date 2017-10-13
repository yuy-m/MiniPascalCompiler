package enshud.s3.checker.ast;

import java.util.List;
import java.util.Objects;

import enshud.s3.checker.type.IType;


public class VariableDeclaration implements IDeclaration
{
    final NameList    names;
    final TypeLiteral type;

    public VariableDeclaration(NameList names, TypeLiteral type)
    {
        this.names = Objects.requireNonNull(names);
        this.type = Objects.requireNonNull(type);
    }

    public List<Identifier> getNames()
    {
        return names.getList();
    }

    public TypeLiteral getTypeLiteral()
    {
        return type;
    }

    public IType getType()
    {
        return type.getType();
    }

    @Override
    public int getLine()
    {
        return names.getLine();
    }

    @Override
    public int getColumn()
    {
        return names.getColumn();
    }

    @Override
    public String toString()
    {
        return "";
    }

    @Override
    public void printBodyln(String indent)
    {
        type.println(indent + " |");
        names.println(indent + "  ");
    }
}


