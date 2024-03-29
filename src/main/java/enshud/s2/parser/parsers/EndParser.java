package enshud.s2.parser.parsers;

import java.util.Objects;
import java.util.Set;

import enshud.s1.lexer.TokenType;
import enshud.s2.parser.ParserInput;
import enshud.s2.parser.node.FailureNode;
import enshud.s2.parser.node.INode;


class EndParser implements IParser
{
    private final IParser parser;
    
    EndParser(IParser parser)
    {
        this.parser = Objects.requireNonNull(parser);
    }
    
    @Override
    public Set<TokenType> getFirstSet()
    {
        return parser.getFirstSet();
    }
    
    @Override
    public INode parse(ParserInput input)
    {
        final INode n = parser.parse(input);
        if (input.isEmpty() || n.isFailure())
        {
            return n;
        }
        else
        {
            return new FailureNode(input.getFront(), "Expected EOF.");
        }
    }
}
