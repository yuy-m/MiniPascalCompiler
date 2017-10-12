package enshud.s2.parser.parsers.basic;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;

import enshud.s2.parser.parsers.IParser;
import enshud.s1.lexer.TokenType;
import enshud.s2.parser.ParserInput;
import enshud.s2.parser.node.INode;
import enshud.s2.parser.node.basic.SequenceNode;

class SequenceParser implements IParser {
	private final IParser[] parsers;

	SequenceParser(IParser[] parsers) {
		this.parsers = Objects.requireNonNull(parsers);
	}

	@Override
	public Set<TokenType> getFirst() {
		return parsers[0].getFirst();
	}

	@Override
	public INode parse(ParserInput input) {
		final int save = input.getIndex();
		final List<INode> childs = new ArrayList<>();

		IParser.verbose("(&");

		for (final IParser parser : parsers) {
			final INode n = parser.parse(input);
			if (n.isSuccess()) {
				childs.add(n);
			} else {
				IParser.verboseln("!)");
				input.setIndex(save);
				return n;
			}

		}
		IParser.verboseln(")");

		return new SequenceNode(childs);
	}
}
