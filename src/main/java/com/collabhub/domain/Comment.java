package com.collabhub.domain;

public class Comment extends BaseEntity {

    private String content;
    private User author;

    public Comment(String content, User author) {
        super();
        this.content = content;
        this.author = author;
    }

    public String getContent() { return content; }
    public User getAuthor() { return author; }

    public void setContent(String content) { this.content = content; }

    @Override
    public String toString() {
        return "Comment{" + super.toString() +
                ", author='" + author.getName() + '\'' +
                ", content='" + content + '\'' + '}';
    }
}