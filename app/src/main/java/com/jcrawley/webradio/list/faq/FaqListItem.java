package com.jcrawley.webradio.list.faq;

public class FaqListItem {

    private final int questionId, answerId;

    public FaqListItem(int questionId, int answerId){
        this.questionId = questionId;
        this.answerId = answerId;
    }

    public int getQuestionId(){
        return questionId;
    }

    public int getAnswerId(){
        return answerId;
    }
}
