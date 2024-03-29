package enshud.s2.parser.parsers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Map;

import enshud.s1.lexer.LexedToken;
import enshud.s1.lexer.TokenType;
import enshud.s2.parser.ParserInput;
import enshud.s2.parser.node.EmptyNode;
import enshud.s2.parser.node.FailureNode;
import enshud.s2.parser.node.INode;
import enshud.s2.parser.node.TokenNode;


class TokenParser implements IParser
{
    private final TokenType                          type;
    
    private static final Map<TokenType, TokenParser> memo = new HashMap<>();
    
    private TokenParser(TokenType type)
    {
        this.type = Objects.requireNonNull(type);
    }
    
    public static TokenParser create(TokenType type)
    {
        if (memo.containsKey(type))
        {
            return memo.get(type);
        }
        else
        {
            return new TokenParser(type);
        }
    }
    
    @Override
    public Set<TokenType> getFirstSet()
    {
        final Set<TokenType> set = new HashSet<>();
        set.add(type);
        return set;
    }
    
    @Override
    public INode parse(ParserInput input)
    {
        if (input.isEmpty())
        {
            return new FailureNode(new EmptyNode(), type + " expected.");
        }
        
        final LexedToken token = input.getFront();
        final INode n = new TokenNode(token);
        
        IParser.verbose("[" + type + ":" + token.getType());
        
        if (type == token.getType())
        {
            IParser.verboseln("]");
            input.popFront();
            // System.err.println(token);
            return n;
        }
        IParser.verboseln("!]");
        // System.err.println(token + ": expected " + type);
        return new FailureNode(n, type + " expected.");
    }
}
