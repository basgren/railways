package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Basil Gren
 *         on 09.12.2014.
 */
public abstract class TextChunkHighlighter {


    public List<TextChunk> highlight(List<TextChunk> textChunks,
                                     String highlightSubstr) {

        highlightSubstr = highlightSubstr.trim();
        ArrayList<TextChunk> result = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for(TextChunk t: textChunks)
            sb.append(t.getText());

        // First, find all substring regions to be highlighted.
        List<TextRegion> regions = findSubstringRegions(sb.toString(), highlightSubstr);
        if (regions == null)
            return textChunks;

        // Now go through every TextChunk and break it down if it intersects
        // with any region. Token type is preserved.
        for(TextChunk chunk: textChunks)
            highlightChunk(chunk, regions, result);

        return result;
    }


    /**
     * Finds all regions of provided string which contain provided substring.
     * Returns array of arrays. Each item is an array that contains begin and
     * end offsets of a substring:
     *
     * @param s String which will be searched for substring.
     * @param subStr Substring to find.
     * @return Array of substring regions (begin and end offsets) or null if
     *         specified substring is empty.
     */
    private List<TextRegion> findSubstringRegions(String s, String subStr) {
        // Prevent infinite loop
        if (subStr.equals(""))
            return null;

        int startOffset = 0, endOffset;
        ArrayList<TextRegion> regions = new ArrayList<>();

        while(startOffset != -1) {
            startOffset = s.indexOf(subStr, startOffset);

            if (startOffset != -1) {
                endOffset = startOffset + subStr.length();
                regions.add(new TextRegion(startOffset, endOffset));
                startOffset = endOffset;
            }
        }

        return regions;
    }


    /**
     * Creates a set of chunks from the passed one. New chunks are determined
     * by intersection with passed regions, so that every new chunk that is
     * inside a region is marked as highlighted.
     *
     * @param chunk Text chunk.
     * @param highlightedRegions A list of regions in original string, which
     *                           should be highlighted.
     * @param chunkList Target chunk collection, that will receive new chunks.
     */
    private void highlightChunk(TextChunk chunk,
                                       List<TextRegion> highlightedRegions,
                                       Collection<TextChunk> chunkList) {
        int newChunkSize;
        int offsRel = 0; // Offset relative to current text chunk

        // We assume that regions are sorted.
        for(TextRegion region: highlightedRegions) {
            // Absolute offset - offset in original string, which text chunk belongs to.
            int offsAbs = chunk.getBeginOffset() + offsRel;

            // Skip to the next region if current does not intersect with chunk
            if (region.endOffset <= offsAbs ||
                    chunk.getEndOffset() < region.startOffset)
                continue;

            // Get intersection of chunk and region
            int intersectionBegin = Math.max(offsAbs, region.startOffset);
            int intersectionEnd = Math.min(chunk.getEndOffset(), region.endOffset);

            // Now breakdown chunk into parts.
            // 1st part - between chunk begin and intersection begin
            newChunkSize = intersectionBegin - offsAbs;
            if (newChunkSize > 0) {
                chunkList.add(createChunk(
                        chunk.getText().substring(offsRel, offsRel + newChunkSize),
                        chunk.getType(), offsAbs));
                offsRel += newChunkSize;
            }

            // 2nd part - intersection itself (highlighted part).
            newChunkSize = intersectionEnd - intersectionBegin;
            if (newChunkSize > 0) {
                TextChunk hlToken = createChunk(
                        chunk.getText().substring(offsRel, offsRel + newChunkSize),
                        chunk.getType(), intersectionBegin);
                hlToken.setHighlighted(true);

                chunkList.add(hlToken);
                offsRel += newChunkSize;
            }
        }

        // the last part - between intersection and chunk end, if it's necessary
        newChunkSize = chunk.getText().length() - offsRel;
        if (newChunkSize > 0) {
            chunkList.add(createChunk(
                    chunk.getText().substring(offsRel, offsRel + newChunkSize),
                    chunk.getType(), chunk.getBeginOffset() + offsRel));
        }
    }


    @NotNull
    protected abstract TextChunk createChunk(@NotNull String text,
                                             int chunkType, int offsetAbs);


    private static class TextRegion {
        public int startOffset;
        public int endOffset;

        public TextRegion(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
    }

}
