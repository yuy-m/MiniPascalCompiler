package enshud.s1.lexer;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Lexer {
	/**
	 * サンプルmainメソッド． 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
		new Lexer().run("data/pas/normal01.pas", "tmp/out1.ts");
		new Lexer().run("data/pas/normal02.pas", "tmp/out2.ts");
	}

	/**
	 * 開発対象となるLexer実行メソッド． 以下の仕様を満たすこと．
	 * 
	 * 仕様: 第一引数で指定されたpasファイルを読み込み，トークン列に分割する． トークン列は第二引数で指定されたtsファイルに書き出すこと．
	 * 正常に処理が終了した場合は標準出力に"OK"を， 入力ファイルが見つからない場合は標準エラーに"File not
	 * found"と出力して終了すること．
	 * 
	 * @param inputFileName
	 *            入力pasファイル名
	 * @param outputFileName
	 *            出力tsファイル名
	 */
	public void run(final String inputFileName, final String outputFileName) {
		if (!lexFromFile(inputFileName)) {
			return;
		}
		outputToFile(outputFileName);
		System.out.println("OK");
	}

	private List<LexedToken> tokens;

	/**
	 * 第一引数で指定されたpasファイルを読み込み，トークン列に分割する．
	 * 
	 * @param input_file
	 *            入力pasファイル名
	 * @return 成功したか？
	 */
	public boolean lexFromFile(String input_file) {
		List<String> lines = importFromFile(input_file);
		if (lines != null) {
			return lex(lines);
		} else {
			return false;
		}
	}

	private static List<String> importFromFile(String input_file) {
		try {
			return Files.readAllLines(Paths.get(input_file));
		} catch (IOException e) {
			System.err.println("File not found");
			return null;
		}
	}

	private boolean lex(List<String> lines) {
		int line_cnt = 1;
		boolean is_in_comment = false;

		tokens = new ArrayList<>();

		try {
			for (final String line : lines) {
				is_in_comment = lexLine(line, line_cnt, is_in_comment);
				++line_cnt;
			}
			return true;
		} catch (IllegalCharException e) {
			System.out.println(e);
			tokens = null;
			return false;
		}
	}

	private boolean lexLine(final String line, final int line_cnt, boolean is_in_comment) throws IllegalCharException {
		int col_idx = 0;

		while (col_idx < line.length()) {
			// when inside comment
			if (is_in_comment) {
				final Token token = TokenType.SCOMMENTEND.lex(line.substring(col_idx));
				if (token != null) {
					is_in_comment = false; // finish ignoring
				}

				// next
				++col_idx;
			}

			// when NOT inside comment
			if (!is_in_comment) {
				final Token token = TokenType.lexOneToken(line.substring(col_idx));
				switch (token.type) {
				case SSPACE: // simply ignore
					break;

				case SCOMMENTBEG: // start ignoring
					is_in_comment = true;
					break;

				case SCOMMENTEND:
				case SUNKNOWN:
					throw new IllegalCharException(line, new LexedToken(token, line_cnt, col_idx + 1));

				default:
					tokens.add(new LexedToken(token, line_cnt, col_idx + 1));
					break;
				}

				// next
				col_idx += token.str.length();
			}
		}

		return is_in_comment;
	}

	/**
	 * 分割したトークン列を第一引数で指定されたファイルに書き出す．
	 * 
	 * @param output_name
	 *            出力tsファイル名
	 */
	public void outputToFile(String output_name) {
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(output_name)))) {
			tokens.forEach(writer::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		String str = "";
		for (final LexedToken t : tokens) {
			str += t + System.lineSeparator();
		}
		return str;
	}

	public static List<LexedToken> importLexedFile(String input_file) {
		List<String> lines = importFromFile(input_file);
		if(lines != null){
			List<LexedToken> tokens = new ArrayList<>();
			for(String s: lines)
			{
				tokens.add(LexedToken.fromString(s));
			}
			return tokens;
		}
		return null;
	}

}
