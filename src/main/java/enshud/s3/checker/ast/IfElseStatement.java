package enshud.s3.checker.ast;

import java.util.Objects;

import enshud.s3.checker.Checker;
import enshud.s3.checker.Procedure;
import enshud.s3.checker.type.IType;
import enshud.s4.compiler.LabelGenerator;


public class IfElseStatement extends IfStatement
{
    StatementList else_statements;

    public IfElseStatement(Expression cond, StatementList then_statements, StatementList else_statements)
    {
        super(cond, then_statements);
        this.else_statements = Objects.requireNonNull(else_statements);
    }

    public StatementList getElse()
    {
        return else_statements;
    }

    @Override
    public IType check(Procedure proc, Checker checker)
    {
        super.check(proc, checker);
        getElse().check(proc, checker);
        return null;
    }

    @Override
    public void compile(StringBuilder codebuilder, Procedure proc, LabelGenerator l_gen)
    {
        final String label = l_gen.toString();
        l_gen.next();

        cond.compile(codebuilder, proc, l_gen);

        codebuilder.append(" LD GR2,GR2").append(System.lineSeparator());
        codebuilder.append(" JZE E").append(label).append("; branch of IF").append(System.lineSeparator());

        then_statements.compile(codebuilder, proc, l_gen);
        codebuilder.append(" JUMP F").append(label).append(System.lineSeparator());

        codebuilder.append("E").append(label).append(" NOP; else of IF").append(System.lineSeparator());
        else_statements.compile(codebuilder, proc, l_gen);

        codebuilder.append("F").append(label).append(" NOP; end of IF").append(System.lineSeparator());
    }

    @Override
    public String toString()
    {
        return "Then & Else";
    }

    @Override
    public void printBodyln(String indent)
    {
        super.printBodyln(indent);
        else_statements.println(indent + "  ", "Else of IfElse");
    }
}