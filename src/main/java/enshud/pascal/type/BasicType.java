package enshud.pascal.type;


public enum BasicType implements IType
{
    INTEGER,
    CHAR {
        @Override
        public boolean equals(IType rval)
        {
            return super.equals(rval) || rval.isArrayOf(BasicType.CHAR);
        }
    },
    BOOLEAN;
    
    @Override
    public int getSize()
    {
        return 1;
    }
    
    @Override
    public final boolean isArrayOf(BasicType btype)
    {
        return false;
    }
    
    @Override
    public final boolean isBasicType()
    {
        return true;
    }
    
    @Override
    public boolean equals(IType rval)
    {
        return rval == this || rval == UnknownType.UNKNOWN;
    }
    
    @Override
    public boolean isArrayType()
    {
        return false;
    }
    
    @Override
    public boolean isUnknown()
    {
        return false;
    }
}

