package enshud.s4.compiler.tacode.io;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import enshud.s4.compiler.tacode.AbstractTAInst;

public class ReadLn extends AbstractTAInst
{
    public ReadLn(String label)
    {
        setLabel(label);
    }
    public ReadLn()
    {
        this(null);
    }
    
    @Override
    public Optional<String> getAssigned()
    {
        return Optional.empty();
    }
    
    @Override
    public Set<String> getRefered()
    {
        return new HashSet<>();
    }
    @Override
    public String toString()
    {
        return getLabel() + "\tReadLn";
    }
}
