package au.com.cybersearch2.classyfy.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

public class SqlParser
{
    public List<String> parseStream(InputStream is) throws IOException
    {
        ArrayList<String> sqlList = new ArrayList<String>();
        Reader r = new BufferedReader(new InputStreamReader(is));
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars(128 + 32, 255);
        st.whitespaceChars(0, 31); // ' ' is not whitespace so it will be inserted in the text
        st.eolIsSignificant(false);
        int tok = StreamTokenizer.TT_EOF;
        StringBuffer buff = new StringBuffer();
        int quote = 0;
        do 
        {
            tok = st.nextToken();
            switch (tok)
            {
            case StreamTokenizer.TT_EOF:
            case StreamTokenizer.TT_EOL:
                break;
            case StreamTokenizer.TT_WORD:
                if (quote == 2)
                {
                    quote = 1;
                    buff.append('\'');
                }
                buff.append(st.sval);
                break;
            case '\'':
                ++quote;
                buff.append('\'');
                break;
            case ',':
            case ')':
                if (quote == 2)
                    quote = 0;
                buff.append((char)tok);
                break;
           default:
                if (tok == ';')
                {
                    quote = 0;
                    buff.append(';');
                    sqlList.add(buff.toString());
                    buff.setLength(0);
                }
                else
                {
                    if (quote == 2)
                    {
                        quote = 1;
                        buff.append('\'');
                    }
                    buff.append((char)tok);
                }
            }
        } while (tok != StreamTokenizer.TT_EOF);
        return sqlList;
    }

}
