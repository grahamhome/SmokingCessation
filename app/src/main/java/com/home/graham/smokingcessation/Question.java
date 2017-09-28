package com.home.graham.smokingcessation;

/**
 * POJO representing a survey question to be answered with a short text response.
 */
class Question implements Comparable<Question> {
    private String name;
    private int position;
    private String text;

    public Question(String name, int position, String text) {
        this.name = name;
        this.position = position;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    @Override
    public int compareTo(Question question) {
        return (this.getPosition() > question.getPosition() ? 1 : this.getPosition() < question.getPosition() ? -1 : 0);
    }
}
