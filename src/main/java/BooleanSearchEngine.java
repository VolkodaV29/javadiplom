import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> wordList = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {

        List<File> listOfPdfFiles = List.of(Objects.requireNonNull(pdfsDir.listFiles()));

        for (File pdf : listOfPdfFiles) {
            var doc = new PdfDocument(new PdfReader(pdf));
            int numberOfPages = doc.getNumberOfPages();

            for (int i = 1; i <= numberOfPages; i++) {
                PdfPage page = doc.getPage(i);
                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");

                Map<String, Integer> freqs = new HashMap<>();
                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    freqs.put(word.toLowerCase(), freqs.getOrDefault(word, 0) + 1);
                }

                for (Map.Entry<String, Integer> f : freqs.entrySet()) {
                    List<PageEntry> pageEntryList = new ArrayList<>();
                    if (wordList.containsKey(f.getKey())) {
                        pageEntryList = wordList.get(f.getKey());
                    }
                    PageEntry pageEntry = new PageEntry(pdf.getName(), i, f.getValue());
                    pageEntryList.add(pageEntry);
                    Collections.sort(pageEntryList);
                    wordList.put(f.getKey(), pageEntryList);
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        String wordToLowerCase = word.toLowerCase();
        if (wordList.containsKey(wordToLowerCase)) {
            return wordList.get(wordToLowerCase);
        }
        return Collections.emptyList();
    }
}