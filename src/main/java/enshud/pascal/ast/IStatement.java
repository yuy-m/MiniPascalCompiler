package enshud.pascal.ast;

import enshud.s3.checker.IChecker;
import enshud.s3.checker.IPrecomputable;
import enshud.s4.compiler.ICompiler;
import enshud.s2.parser.node.INode;


public interface IStatement extends IASTNode, IChecker, IPrecomputable, ICompiler
{
    @Override
    default void printHead(String indent, String msg)
    {
        System.out.print("-");
        INode.printPurple(getClass().getSimpleName());
        System.out.print(": ");
        INode.printGreenln(msg != null? msg: toString());
    }
}
