package com.urbanise;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyAnalysis 
{

    private class CyclicDependencyException extends Throwable {
    }

    // I implement Comparable because I want a nice sorted list when printing.
    class Token implements Comparable<Token> {
        Set<Token> children = new HashSet<Token>();
        String name;

        public Token(String name) {
            this.name = name;
        }

        public boolean addChild(Token token) {
            if (token.allDescendants().contains(this)) {
                return false;
            } else {
                this.children.add(token);
                return true;
            }
        }

        public Set<Token> allDescendants() {
            Set<Token> all = new TreeSet<>();

            for (Token c : children) {
                all.add(c);
                all.addAll(c.allDescendants());
            }
            return all;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Token token = (Token) o;
            return name != null ? name.equals(token.name) : token.name == null;

        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public String toString() {
            return name + " "+ allDescendants().stream().map(x -> x.name).collect(Collectors.joining(" "));
        }

        @Override
        public int compareTo(Token o) {
            return name.compareTo(o.name);
        }
    }

    private Map<String, Token> tokensMap = new HashMap<String, Token>();

    private void print() {
        for (String name : tokensMap.keySet()) {
            System.out.println(tokensMap.get(name).toString());
        }
    }

    /**
     * Get a token from the Map if it already exists, else create it and put it in the map.
     */
    public Token getOrCreateToken(String tokenName) {
        Token currentToken;
        if (!tokensMap.containsKey(tokenName)) {
            currentToken = new Token(tokenName);
            tokensMap.put(tokenName, currentToken);
        } else {
            currentToken = tokensMap.get(tokenName);
        }
        return currentToken;
    }

    public boolean tryDirectDependency(String rawDependency) throws CyclicDependencyException {
        String[] elements = rawDependency.split(" ");

        String tokenName = elements[0];

        Token token = getOrCreateToken(tokenName);

        for (int i = 1; i < elements.length; i++) {
            if (!token.addChild(getOrCreateToken(elements[i]))) {
                throw new CyclicDependencyException();
            }
        }
        return true;
    }

    public static void main(String args[]) {

        try {
            DependencyAnalysis dependencyAnalysis = new DependencyAnalysis();
            dependencyAnalysis.tryDirectDependency("A B C");
            dependencyAnalysis.tryDirectDependency("B C E");
            dependencyAnalysis.tryDirectDependency("C G");
            dependencyAnalysis.tryDirectDependency("D A F");
            dependencyAnalysis.tryDirectDependency("E F");
            dependencyAnalysis.tryDirectDependency("F H");

            dependencyAnalysis.print();
        } catch (CyclicDependencyException e) {
            e.printStackTrace();
        }

        try {
            DependencyAnalysis dependencyAnalysis = new DependencyAnalysis();
            dependencyAnalysis.tryDirectDependency("A B");
            dependencyAnalysis.tryDirectDependency("B A");
            dependencyAnalysis.print();
        } catch (CyclicDependencyException e) {
            e.printStackTrace();
        }
    }
}