package karsai.laszlo.bringcloser;

import android.content.Context;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;

/**
 * Chunk class is under the following licence:
 *This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation with the addition of the
 following permission added to Section 15 as permitted in Section 7(a):
 FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 OF THIRD PARTY RIGHTS

 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Affero General Public License for more details.
 You should have received a copy of the GNU Affero General Public License
 along with this program; if not, see http://www.gnu.org/licenses or write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 Boston, MA, 02110-1301 USA, or download the license from the following URL:
 http://itextpdf.com/terms-of-use/

 The interactive user interfaces in modified source and object code versions
 of this program must display Appropriate Legal Notices, as required under
 Section 5 of the GNU Affero General Public License.

 In accordance with Section 7(b) of the GNU Affero General Public License,
 a covered work must retain the producer line in every PDF that is created
 or manipulated using iText.

 You can be released from the requirements of the license by purchasing
 a commercial license. Buying such a license is mandatory as soon as you
 develop commercial activities involving the iText software without
 disclosing the source code of your own applications.
 These activities include: offering paid services to customers as an ASP,
 serving PDFs on the fly in a web application, shipping iText with a closed
 source product.

 For more information, please contact iText Software Corp. at this
 address: sales@itextpdf.com

 LICENSE END
 I created this class extending Chunk class because there are some cases when special characters
 cannot be displayed. That is why I created a replacement analysis. After the replacement is done,
 the class of Chunk is used as it is.
 So, my modification is about how the appropriate constructor of Chunk class gets invoked.
 */
public class CustomChunk extends Chunk {

    public CustomChunk(Context context, String content, Font font) {
        super(replaceCharIfNeeded(context, content), font);
    }

    private static String replaceCharIfNeeded(Context context, String content) {
        String charsToReplaceArray [] = context.getResources().getStringArray(R.array.pdf_chars_to_replace);
        String charsReplacement [] = context.getResources().getStringArray(R.array.pdf_chars_replacement);
        for (int i = 0; i < charsToReplaceArray.length; i++) {
            content = content.replaceAll(charsToReplaceArray[i], charsReplacement[i]);
        }
        return content;
    }
}
