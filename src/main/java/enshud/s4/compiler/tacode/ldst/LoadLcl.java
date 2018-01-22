package enshud.s4.compiler.tacode.ldst;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import enshud.pascal.QualifiedVariable;
import enshud.s4.compiler.tacode.AbstractTAInst;

public class LoadLcl extends AbstractTAInst
{
    String to;
    QualifiedVariable var;
    public LoadLcl(String to, QualifiedVariable var, String label)
    {
        this.to = Objects.requireNonNull(to);
        this.var = Objects.requireNonNull(var);
        setLabel(label);
    }
    public LoadLcl(String to, QualifiedVariable var)
    {
        this(to, var, null);
    }
    
    @Override
    public Optional<String> getAssigned()
    {
        return Optional.of(to);
    }
    
    @Override
    public Set<String> getRefered()
    {
        return new HashSet<>();
    }
    
    @Override
    public String toString()
    {
        return String.format("%s\t%s = Lcl(%s)", getLabel(), to, var.getQualifiedName());
    }
}
