package com.kauron.newssarcher;

import android.content.Context;

class Query {
    private final String query;
    private String answer;
    private final boolean noStopwords;
    private final boolean stemming;
    private final Context context;
    private final String host;
    private final int port;

    Query(String query, String host, int port, boolean noStopwords, boolean stemming, Context context) {
        this.query = query;
        this.noStopwords = noStopwords;
        this.stemming = stemming;
        this.context = context;
        this.host = host;
        this.port = port;
    }

    String getEndpoint() {
        return host + ":" + port;
    }

    String getQuery() {
        return query;
    }

    String getAnswer() {
        return answer;
    }

    String getOptions() {
        return String.format("Stemming (%s), Remove stopwords (%s)", stemming ? "yes" : "no",
                noStopwords ? "yes" : "no");
    }

    String getShortOptions() {
        return (stemming ? " -s" : "") + (noStopwords ? " -n" : "");
    }

    String getShortAnswer() {
        if (answer == null)
            return context.getString(R.string.no_answer_yet);
        else
            return answer.substring(answer.lastIndexOf('\n') + 1);
    }

    void setAnswer(String answer) {
        this.answer = answer;
    }

    boolean hasAnswer() {
        return answer == null;
    }
}
