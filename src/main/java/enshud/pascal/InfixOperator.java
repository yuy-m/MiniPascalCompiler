package enshud.pascal;

import enshud.pascal.ast.expression.BooleanLiteral;
import enshud.pascal.ast.expression.IConstant;
import enshud.pascal.ast.expression.IntegerLiteral;
import enshud.pascal.type.BasicType;
import enshud.pascal.type.IType;
import enshud.s1.lexer.LexedToken;
import enshud.s3.checker.Checker;
import enshud.s4.compiler.Casl2Code;
import enshud.s4.compiler.LabelGenerator;


public enum InfixOperator
{
    ADD {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return check_(
                proc, checker, op_tok, givenl, givenr, BasicType.INTEGER, BasicType.INTEGER, BasicType.INTEGER
            );
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new IntegerLiteral(left + right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("ADDA", "", "", "GR2", "GR1");
        }
    },
    SUB {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return check_(
                proc, checker, op_tok, givenl, givenr, BasicType.INTEGER, BasicType.INTEGER, BasicType.INTEGER
            );
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new IntegerLiteral(left - right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("SUBA", "", "", "GR1", "GR2");
            code.add("LD", "", "", "GR2", "GR1");
        }
    },
    MUL {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return check_(
                proc, checker, op_tok, givenl, givenr, BasicType.INTEGER, BasicType.INTEGER, BasicType.INTEGER
            );
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new IntegerLiteral(left * right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("CALL", "", "", "MULT");
        }
    },
    DIV {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return check_(
                proc, checker, op_tok, givenl, givenr, BasicType.INTEGER, BasicType.INTEGER, BasicType.INTEGER
            );
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new IntegerLiteral(left / right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("CALL", "", "", "DIV");
        }
    },
    MOD {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return check_(
                proc, checker, op_tok, givenl, givenr, BasicType.INTEGER, BasicType.INTEGER, BasicType.INTEGER
            );
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new IntegerLiteral(left % right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("CALL", "", "", "DIV");
            code.add("LD", "", "", "GR2", "GR1");
        }
    },
    
    OR {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return check_(
                proc, checker, op_tok, givenl, givenr, BasicType.BOOLEAN, BasicType.BOOLEAN, BasicType.BOOLEAN
            );
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new BooleanLiteral(left == 1 || right == 1);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("OR", "", "", "GR2", "GR1");
        }
    },
    AND {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return check_(
                proc, checker, op_tok, givenl, givenr, BasicType.BOOLEAN, BasicType.BOOLEAN, BasicType.BOOLEAN
            );
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new BooleanLiteral(left == 1 && right == 1);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("AND", "", "", "GR2", "GR1");
        }
    },
    
    EQUAL {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return checkCompareOp(proc, checker, op_tok, givenl, givenr);
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new BooleanLiteral(left == right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            final int label = l_gen.next();
            code.add("CPL", "", "; v =", "GR2", "GR1");
            code.add("JZE", "", "", "Z" + label);
            code.add("XOR", "", "", "GR2", "GR2");
            code.add("JUMP", "", "", "Q" + label);
            code.add("LAD", "Z" + label, "", "GR2", "1");
            code.add("NOP", "Q" + label, "; ^ =");
        }
    },
    NOTEQUAL {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return checkCompareOp(proc, checker, op_tok, givenl, givenr);
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new BooleanLiteral(left != right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            final int label = l_gen.next();
            code.add("CPL", "", "; v <>", "GR2", "GR1");
            code.add("JNZ", "", "", "Z" + label);
            code.add("XOR", "", "", "GR2", "GR2");
            code.add("JUMP", "", "", "Q" + label);
            code.add("LAD", "Z" + label, "", "GR2", "1");
            code.add("NOP", "Q" + label, "; ^ <>");
        }
    },
    LESS {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return checkCompareOp(proc, checker, op_tok, givenl, givenr);
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new BooleanLiteral(left < right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("SUBA", "", "; <", "GR1", "GR2");
            code.add("LD", "", "", "GR2", "GR1");
            code.add("SRL", "", "", "GR2", "15");
        }
    },
    LESSEQUAL {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return checkCompareOp(proc, checker, op_tok, givenl, givenr);
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new BooleanLiteral(left <= right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("SUBA", "", "; <=", "GR2", "GR1");
            code.add("SRL", "", "", "GR2", "15");
            code.add("XOR", "", "", "GR2", "=1");
        }
    },
    GREAT {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return checkCompareOp(proc, checker, op_tok, givenl, givenr);
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new BooleanLiteral(left > right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("SUBA", "", "; >", "GR2", "GR1");
            code.add("SRL", "", "", "GR2", "15");
        }
    },
    GREATEQUAL {
        @Override
        public IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr)
        {
            return checkCompareOp(proc, checker, op_tok, givenl, givenr);
        }
        
        @Override
        public IConstant eval(int left, int right)
        {
            return new BooleanLiteral(left >= right);
        }
        
        @Override
        public void compile(Casl2Code code, LabelGenerator l_gen)
        {
            code.add("SUBA", "", "; >=", "GR1", "GR2");
            code.add("LD", "", "", "GR2", "GR1");
            code.add("SRL", "", "", "GR2", "15");
            code.add("XOR", "", "", "GR2", "=1");
        }
    };
    
    
    public static InfixOperator getFromToken(LexedToken token)
    {
        switch (token.getType())
        {
        case SPLUS:
            return InfixOperator.ADD;
        case SMINUS:
            return InfixOperator.SUB;
        case SSTAR:
            return InfixOperator.MUL;
        case SDIVD:
            return InfixOperator.DIV;
        case SMOD:
            return InfixOperator.MOD;
        case SOR:
            return InfixOperator.OR;
        case SAND:
            return InfixOperator.AND;
        case SEQUAL:
            return InfixOperator.EQUAL;
        case SNOTEQUAL:
            return InfixOperator.NOTEQUAL;
        case SLESS:
            return InfixOperator.LESS;
        case SLESSEQUAL:
            return InfixOperator.LESSEQUAL;
        case SGREAT:
            return InfixOperator.GREAT;
        case SGREATEQUAL:
            return InfixOperator.GREATEQUAL;
        default:
            assert false;
            return null;
        }
    }
    
    public abstract IType checkType(Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr);
    
    protected IType check_(
        Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr, IType expectedl,
        IType expectedr, IType result
    )
    {
        givenl = checkTypeMatch(proc, checker, op_tok, givenl, expectedl);
        givenr = checkTypeMatch(proc, checker, op_tok, givenr, expectedr);
        checkTypeMatch2(proc, checker, op_tok, givenl, givenr, expectedl, expectedr);
        return result;
    }
    
    protected IType checkCompareOp(
        Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr
    )
    {
        if (givenl == BasicType.INTEGER || givenr == BasicType.INTEGER)
        {
            check_(proc, checker, op_tok, givenl, givenr, BasicType.INTEGER, BasicType.INTEGER, BasicType.BOOLEAN);
        }
        else if (givenl == BasicType.BOOLEAN || givenr == BasicType.BOOLEAN)
        {
            check_(proc, checker, op_tok, givenl, givenr, BasicType.BOOLEAN, BasicType.BOOLEAN, BasicType.BOOLEAN);
        }
        else if (givenl == BasicType.CHAR || givenr == BasicType.CHAR)
        {
            check_(proc, checker, op_tok, givenl, givenr, BasicType.CHAR, BasicType.CHAR, BasicType.BOOLEAN);
        }
        return BasicType.BOOLEAN;
    }
    
    private IType checkTypeMatch(Procedure proc, Checker checker, LexedToken op_tok, IType given, IType expected)
    {
        if (given.isUnknown())
        {
            return expected;
        }
        else if (!given.equals(expected))
        {
            checker.addErrorMessage(
                proc, op_tok,
                "incompatible type: cannot use " + given + " type as operand of " + this + " operator. must be "
                        + expected + "."
            );
        }
        return given;
    }
    
    private void checkTypeMatch2(
        Procedure proc, Checker checker, LexedToken op_tok, IType givenl, IType givenr, IType expectedl, IType expectedr
    )
    {
        if (!givenl.isUnknown() && !givenr.isUnknown() && !givenl.equals(givenr))
        {
            checker.addErrorMessage(
                proc, op_tok,
                "incompatible type: left type " + givenl + " differs from right type " + givenr + " of " + this + " operator."
            );
        }
    }
    
    public abstract IConstant eval(int left, int right);
    public abstract void compile(Casl2Code code, LabelGenerator l_gen); // left->GR1,right->GR2
}