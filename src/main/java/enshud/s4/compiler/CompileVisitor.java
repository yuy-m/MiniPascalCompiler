package enshud.s4.compiler;

import java.util.stream.IntStream;

import enshud.pascal.Procedure;
import enshud.pascal.ast.IVisitor;
import enshud.pascal.ast.TemplateVisitor;
import enshud.pascal.ast.expression.*;
import enshud.pascal.ast.statement.*;
import enshud.pascal.type.ArrayType;
import enshud.pascal.type.BasicType;

public class CompileVisitor implements IVisitor<Object, Procedure>
{
    private final Casl2Code code = new Casl2Code();
    private final LabelGenerator l_gen = new LabelGenerator();
    
    public Casl2Code getCode()
    {
        return code;
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteral node, Procedure option)
    {
        code.addLoadImm("GR2", node.getInt());
        return null;
    }
    @Override
    public Object visitIndexedVariable(IndexedVariable node, Procedure proc)
    {
        compileIndexedVariableForData(node, proc);
        return null;
    }
    
    public void compileIndexedVariableForData(IndexedVariable node, Procedure proc)
    {
        compileIndexedVariableImpl(node, proc);
        code.add("LD", "", "", "GR2", "0", "GR1");
    }
    
    public void compileIndexedVariableForAddr(IndexedVariable node, Procedure proc)
    {
        compileIndexedVariableImpl(node, proc);
        code.add("LD", "", "", "GR2", "GR1");
    }
    
    private void compileIndexedVariableImpl(IndexedVariable node, Procedure proc)
    {
        code.add("", "", "; var " + node.getVar().getQualifiedName() + "[]");
        
        node.getIndex().accept(this, proc);
        if (node.getVar().getProc() == proc)
        {
            compileIndexedVariableLocalImpl(node, proc);
        }
        else
        {
            compileIndexedVariableOuterImpl(node, proc);
        }
        code.add("ADDL", "", "; add index", "GR1", "GR2");
    }
    
    private void compileIndexedVariableLocalImpl(IndexedVariable node, Procedure proc)
    {
        final int align = node.getVar().getAlignment();
        final int max = ((ArrayType)node.getVar().getType()).getMax();
        code.add("LAD", "", "", "GR1", "" + (-align - 2 - max), "GR5");
    }
    
    /// proc == null? Local Var: Outer Var
    private void compileIndexedVariableOuterImpl(IndexedVariable node, Procedure proc)
    {
        final int depth_diff = proc.getDepth() - node.getVar().getProc().getDepth() - 1;
        loadStaticLink("GR1", depth_diff);
        
        final int align = node.getVar().getAlignment();
        final int max = ((ArrayType)node.getVar().getType()).getMax();
        code.addAddlImm("GR1", -align - 2 - max);
    }
    
    @Override
    public Object visitInfixOperation(InfixOperation node, Procedure proc)
    {
        if (node.getLeft() instanceof IConstant)
        {
            if (node.getRight() instanceof IConstant)
            {
                code.addLoadImm("GR2", ((IConstant)node.getRight()).getInt());
            }
            else
            {
                node.getRight().accept(this, proc);
            }
            code.addLoadImm("GR1", ((IConstant)node.getLeft()).getInt());
        }
        else
        {
            node.getLeft().accept(this, proc);
            if (node.getRight() instanceof IConstant)
            {
                code.add("LD", "", "", "GR1", "GR2");
                code.addLoadImm("GR2", ((IConstant)node.getRight()).getInt());
            }
            else
            {
                code.add("PUSH", "", "", "0", "GR2");
                node.getRight().accept(this, proc);
                code.add("POP", "", "", "GR1");
            }
        }
        node.getOp().compile(code, l_gen);
        return null;
    }
    @Override
    public Object visitIntegerLiteral(IntegerLiteral node, Procedure proc)
    {
        code.addLoadImm("GR2", node.getInt());
        return null;
    }
    @Override
    public Object visitPrefixOperation(PrefixOperation node, Procedure proc)
    {
        node.getOperand().accept(this, proc);
        node.getOp().compile(code, l_gen);
        return null;
    }
    @Override
    public Object visitPureVariable(PureVariable node, Procedure proc)
    {
        if (node.getType().isBasicType())
        {
            compilePureVariableForData(node, proc);
        }
        else
        {
            compilePureVariableForAddr(node, proc);
        }
        return null;
    }
    public void compilePureVariableForData(PureVariable node, Procedure proc)
    {
        compilePureVariableImpl(node, "LD", proc);
    }
    
    public void compilePureVariableForAddr(PureVariable node, Procedure proc)
    {
        compilePureVariableImpl(node, "LAD", proc);
    }
    
    private void compilePureVariableImpl(PureVariable node, String inst, Procedure proc)
    {
        if (node.isParam())
        {
            code.add("", "", "; param " + node.getVar().getQualifiedName());
            final int align = node.getVar().getAlignment();
            if(node.getVar().getProc() == proc)
            {
                code.add(inst, "", "", "GR2", "" + (align + 2), "GR5");
            }
            else
            {
                final int depth_diff = proc.getDepth() - node.getVar().getProc().getDepth();
                loadStaticLink("GR2", depth_diff - 1);
                code.add(inst, "", "", "GR2", "" + (align + 2), "GR2");
            }
        }
        else
        {
            code.add("", "", "; var " + node.getVar().getQualifiedName());
            if (node.getVar().getProc() == proc)
            {
                compilePureVariableForLocal(node, null, inst, "GR5");
            }
            else
            {
                compilePureVariableForLocal(node, proc, inst, "GR1");
            }
        }
    }
    
    /// proc == null? Local Var: Outer Var
    private void compilePureVariableForLocal(PureVariable node, Procedure proc, String inst, String gr)
    {
        final int align = node.getVar().getAlignment();
        
        if (proc != null) // go back stack frame by static link
        {
            final int depth_diff = proc.getDepth() - node.getVar().getProc().getDepth() - 1;
            loadStaticLink(gr, depth_diff);
        }
        
        if (node.getVar().getType().isArrayType())
        {
            final int len = ((ArrayType)node.getVar().getType()).getSize();
            code.add(inst, "", "", "GR2", "" + (-align - 1 - len), gr);
            code.addLoadImm("GR1", node.getVar().getType().getSize()); // array length
        }
        else
        {
            code.add(inst, "", "", "GR2", "" + (-align - 2), gr);
        }
    }

    @Override
    public Object visitStringLiteral(StringLiteral node, Procedure proc)
    {
        if (node.getType() == BasicType.CHAR)
        {
            code.addLoadImm("GR2", (int)node.toString().charAt(1));
        }
        else
        {
            code.add("LAD", "", "", "GR2", "=" + node.toString());
            code.addLoadImm("GR1", node.length());
        }
        return null;
    }

    @Override
    public Object visitAssignStatement(AssignStatement node, Procedure proc)
    {
        node.getLeft().accept(address_visitor, proc);
        if(node.getRight() instanceof IConstant)
        {
            code.add("LD", "", "", "GR1", "GR2");
            code.addLoadImm("GR2", ((IConstant)node.getRight()).getInt());
        }
        else
        {
            code.add("PUSH", "", "", "0", "GR2");
            node.getRight().accept(this, proc);
            code.add("POP", "", "", "GR1");
        }
        code.add("ST", "", "", "GR2", "0", "GR1");
        return null;
    }

    @Override
    public Object visitCompoundStatement(CompoundStatement node, Procedure proc)
    {
        node.forEach(stm -> stm.accept(this, proc));
        return null;
    }
    
    @Override
    public Object visitIfElseStatement(IfElseStatement node, Procedure proc)
    {
        final int label = l_gen.next();
        
        node.getCond().accept(this, proc);
        
        code.add("LD", "", "; set ZF", "GR2", "GR2");
        code.add("JZE", "", "; branch of IF", "E" + label);
        
        node.getThen().accept(this, proc);
        code.add("JUMP", "", "", "F" + label);
        
        code.add("NOP", "E" + label, "; else of IF");
        node.getElse().accept(this, proc);
        
        code.add("NOP", "F" + label, "; end of IF");
        return null;
    }
    
    @Override
    public Object visitIfStatement(IfStatement node, Procedure proc)
    {
        final int label = l_gen.next();
        
        node.getCond().accept(this, proc);
        
        code.add("LD", "", "; set ZF", "GR2", "GR2");
        code.add("JZE", "", "; branch of IF", "F" + label);
        
        node.getThen().accept(this, proc);

        code.add("NOP", "F" + label, "; end of IF");
        return null;
    }
    
    @Override
    public Object visitProcCallStatement(ProcCallStatement node, Procedure proc)
    {
        compileArguments(node, proc);
        compileStaticFramePointer(node, proc);
        
        //f(code, proc.getName().charAt(0), '[');
        
        code.add("CALL", "", "; proc " + node.getCalledProc().getQualifiedName(), node.getCalledProc().getId());
        
        //f(code, proc.getName().charAt(0), ']');
        
        // remove rest child frame
        code.addAddlImm("GR8", node.getArgs().size() + 1);
        return null;
    }
    
    private void compileArguments(ProcCallStatement node, Procedure proc)
    {
        for (int i = node.getArgs().size() - 1; i >= 0; --i)
        {
            final IExpression e = node.getArgs().get(i);
            if (e instanceof IConstant)
            {
                code.add("PUSH", "", "", "" + ((IConstant)e).getInt());
            }
            else
            {
                node.getArgs().get(i).accept(this, proc);
                code.add("PUSH", "", "", "0", "GR2");
            }
        }
    }
    
    private void compileStaticFramePointer(ProcCallStatement node, Procedure proc)
    {
        final int my_depth = proc.getDepth();
        final int your_depth = node.getCalledProc().getDepth();
        if (my_depth + 1 == your_depth) // child
        {
            code.add("PUSH", "", "", "0", "GR5");
        }
        else if (my_depth >= your_depth) // ancestor or recursive call
        {
            loadStaticLink("GR2", my_depth - your_depth);
            
            code.add("PUSH", "", "", "0", "GR2");
        }
        else
        {
            new Exception(proc.getQualifiedName() + " cannot call " + node.getCalledProc().getQualifiedName());
        }
    }
    private void loadStaticLink(String gr, int diff)
    {
        code.add("LD", "", "", gr, "1", "GR5");
        IntStream.range(0, diff)
            .forEach(i -> code.add("LD", "", "", gr, "1", gr));
    }
    
    @Override
    public Object visitReadStatement(ReadStatement node, Procedure proc)
    {
        if (node.getVariables().isEmpty())
        {
            code.add("CALL", "", "", "RDLN");
            return null;
        }
        
        node.getVariables().forEach(
            v -> {
                v.accept(address_visitor, proc);
                if (v.getType() == BasicType.CHAR)
                {
                    code.add("CALL", "", "", "RDCH");
                }
                else if (v.getType() == BasicType.INTEGER)
                {
                    code.add("CALL", "", "", "RDINT");
                }
                else if (v.getType().isArrayOf(BasicType.CHAR))
                {
                    code.add("CALL", "", "", "RDSTR");
                }
                else
                {
                    assert false: "type error";
                }
            }
        );
        return null;
    }
    
    @Override
    public Object visitWhileStatement(WhileStatement node, Procedure proc)
    {
        final int label = l_gen.next();
        
        code.add("NOP", "C" + label, "; start of WHILE");
        
        if (!node.isInfiniteLoop())
        {
            node.getCond().accept(this, proc);

            code.add("LD", "",                   "", "GR2", "GR2");
            code.add("JZE", "", "; branch of WHILE", "F" + label);
        }
        
        node.getStatement().accept(this, proc);
        code.add("JUMP", "", "", "C" + label);
        
        if (!node.isInfiniteLoop())
        {
            code.add("NOP", "F" + label, "; end of WHILE");
        }
        return null;
    }
    
    @Override
    public Object visitWriteStatement(WriteStatement node, Procedure proc)
    {
        for (final IExpression e: node.getExpressions())
        {
            e.accept(this, proc);
            
            if (e.getType() == BasicType.CHAR)
            {
                code.add("CALL", "", "", "WRTCH");
            }
            else if (e.getType() == BasicType.INTEGER)
            {
                code.add("CALL", "", "", "WRTINT");
            }
            else if (e.getType().isArrayOf(BasicType.CHAR))
            {
                code.add("CALL", "", "", "WRTSTR");
            }
            else
            {
                assert false: "type error: (" + e.getLine() + "," + e.getColumn() + ")" + e.getType();
            }
        }
        code.add("CALL", "", "", "WRTLN");
        return null;
    }
    
    final IVisitor<Object, Procedure> address_visitor = new TemplateVisitor<Object, Procedure>() {
        @Override
        public Object visitIndexedVariable(IndexedVariable node, Procedure proc)
        {
            compileIndexedVariableForAddr(node, proc);
            return null;
        }
        @Override
        public Object visitPureVariable(PureVariable node, Procedure proc)
        {
            compilePureVariableForAddr(node, proc);
            return null;
        }
        
    };
}
