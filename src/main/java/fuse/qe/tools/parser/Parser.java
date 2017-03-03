/**
 * Copyright 2011 Alexandre Dutra
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package fuse.qe.tools.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Parser {
	protected int lineIndex = 0;

    protected List<String> lines;

    protected List<String> splitLines(final Reader reader) throws IOException {
        String line = null;
        final BufferedReader br;
        if(reader instanceof BufferedReader) {
            br = (BufferedReader) reader;
        } else {
            br = new BufferedReader(reader);
        }
        final List<String> lines = new ArrayList<String>();
        while((line = br.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    /**
     * When doing an install at the same time on a multi-module project, one can get this kind of output:
     * <pre>
     * +- active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = com.acme.org:foobar:jar:1.0.41-SNAPSHOT:compile;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml
     * </pre>
     */
    protected String extractActiveProjectArtifact() {
        String artifact = null;
        //start at next line and consume all lines containing "artifact =" or "project: "; record the last line containing "artifact =".
        boolean artifactFound = false;
        while(this.lineIndex < this.lines.size() - 1) {
            String tempLine = this.lines.get(this.lineIndex + 1);
            boolean artifactLine = !artifactFound && tempLine.contains("artifact = ");
            boolean projectLine = artifactFound && tempLine.contains("project: ");
            if(artifactLine || projectLine) {
                if(tempLine.contains("artifact = ") && ! tempLine.contains("active project artifact:")) {
                    artifact = StringUtils.substringBefore(StringUtils.substringAfter(tempLine, "artifact = "), ";");
                    artifactFound = true;
                }
                this.lineIndex++;
            } else {
                break;
            }
        }
        return artifact;
    }

    /**
     * Parses a string representing a Maven artifact in standard notation.
     * @param artifact
     * @return an instance of {@link Node} representing the artifact.
     */
    protected Node parseArtifactString(final String artifact) {
        final List<String> tokens = new ArrayList<String>(7);
        int tokenStart = 0;
        boolean tokenStarted = false;
        boolean hasDescription = false;
        boolean omitted = false;
        int tokenEnd = 0;
        for (; tokenEnd < artifact.length(); tokenEnd++) {
            final char c = artifact.charAt(tokenEnd);
            switch (c){
                case ' ': // in descriptions only
                    if(tokenStarted && ! hasDescription) {
                        tokens.add(artifact.substring(tokenStart, tokenEnd));
                        tokenStarted = false;
                        hasDescription = true;
                    }
                    continue;

                case ':':
                case ')': //end of descriptions and omitted artifacts
                    tokens.add(artifact.substring(tokenStart, tokenEnd));
                    tokenStarted = false;
                    continue;

                case '-': // in omitted artifacts descriptions
                    continue;

                case '(': // in omitted artifacts
                    if(tokenEnd == 0) {
                        omitted = true;
                    }
                    continue;

                default:
                    if(! tokenStarted) {
                        tokenStart = tokenEnd;
                        tokenStarted = true;
                    }
                    continue;
            }
        }

        //last token
        if(tokenStarted) {
            tokens.add(artifact.substring(tokenStart, tokenEnd));
        }

        String groupId;
        String artifactId;
        String packaging;
        String classifier;
        String version;
        String scope;
        String description;

        if(tokens.size() == 4) {

            groupId = tokens.get(0);
            artifactId = tokens.get(1);
            packaging = tokens.get(2);
            version = tokens.get(3);
            scope = null;
            description = null;
            classifier = null;

        } else if(tokens.size() == 5) {

            groupId = tokens.get(0);
            artifactId = tokens.get(1);
            packaging = tokens.get(2);
            version = tokens.get(3);
            scope = tokens.get(4);
            description = null;
            classifier = null;

        } else if(tokens.size() == 6) {

            if(hasDescription) {
                groupId = tokens.get(0);
                artifactId = tokens.get(1);
                packaging = tokens.get(2);
                version = tokens.get(3);
                scope = tokens.get(4);
                description = tokens.get(5);
                classifier = null;
            } else {
                groupId = tokens.get(0);
                artifactId = tokens.get(1);
                packaging = tokens.get(2);
                classifier = tokens.get(3);
                version = tokens.get(4);
                scope = tokens.get(5);
                description = null;
            }

        } else if(tokens.size() == 7) {

            groupId = tokens.get(0);
            artifactId = tokens.get(1);
            packaging = tokens.get(2);
            classifier = tokens.get(3);
            version = tokens.get(4);
            scope = tokens.get(5);
            description = tokens.get(6);

        } else {
            throw new IllegalStateException("Wrong number of tokens: " + tokens.size() + " for artifact: " + artifact);
        }

        final Node node = new Node(
            groupId,
            artifactId,
            packaging,
            classifier,
            version,
            scope,
            description,
            omitted
        );
        return node;
    }
    
    public Node parse(Reader reader) {
        try {
			this.lines = splitLines(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
        

        if(lines.isEmpty()) {
            return null;
        }

        return parseInternal(0);

    }

    private Node parseInternal(final int depth){

        //current node
        final Node node = this.parseLine();

        this.lineIndex++;

        //children
        while (this.lineIndex < this.lines.size() && this.computeDepth(this.lines.get(this.lineIndex)) > depth) {
            final Node child = this.parseInternal(depth + 1);
            if(node != null) {
                node.addChildNode(child);
            }
        }
        return node;

    }

    private int computeDepth(final String line) {
        return getArtifactIndex(line)/3;
    }

    /**
     * sample lineIndex structure:
     * <pre>|  |  \- org.apache.activemq:activeio-core:test-jar:tests:3.1.0:compile</pre>
     * @return
     */
    private Node parseLine() {
        String line = this.lines.get(this.lineIndex);
        String artifact;
        if(line.contains("active project artifact:")) {
            artifact = extractActiveProjectArtifact();
        } else {
            artifact = extractArtifact(line);
        }
        return parseArtifactString(artifact);
    }

    private String extractArtifact(String line) {
        return line.substring(getArtifactIndex(line));
    }

    private int getArtifactIndex(final String line) {
        for (int i = 0; i < line.length(); i++) {
            final char c = line.charAt(i);
            switch (c){
                case ' '://whitespace, standard and extended
                case '|'://standard
                case '+'://standard
                case '\\'://standard
                case '-'://standard
                case '³'://extended
                case 'Ã'://extended
                case 'Ä'://extended
                case 'À'://extended
                    continue;
                default:
                    return i;
            }
        }
        return -1;
    }


}
