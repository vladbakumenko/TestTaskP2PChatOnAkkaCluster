package ru.vladbakumenko.ui.chat;


import ru.vladbakumenko.dto.ChanelCompound;
import ru.vladbakumenko.dto.GroupMessage;
import ru.vladbakumenko.dto.PrivateMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.vladbakumenko.App.GROUP_CHAT_NAME;

public class ChatControllerModel {
    private List<GroupMessage> historyOfGroupMessages = new ArrayList<>();
    private Map<ChanelCompound, List<PrivateMessage>> historyOfPrivateMessages = new HashMap<>();
    private String nameOfChat = GROUP_CHAT_NAME;

    public String getNameOfChat() {
        return nameOfChat;
    }

    public void setNameOfChat(String nameOfChat) {
        this.nameOfChat = nameOfChat;
    }

    public List<GroupMessage> getHistoryOfGroupMessages() {
        return historyOfGroupMessages;
    }

    public void setHistoryOfGroupMessages(List<GroupMessage> historyOfGroupMessages) {
        this.historyOfGroupMessages = historyOfGroupMessages;
    }

    public Map<ChanelCompound, List<PrivateMessage>> getHistoryOfPrivateMessages() {
        return historyOfPrivateMessages;
    }

    public void setHistoryOfPrivateMessages(Map<ChanelCompound, List<PrivateMessage>> historyOfPrivateMessages) {
        this.historyOfPrivateMessages = historyOfPrivateMessages;
    }
}
