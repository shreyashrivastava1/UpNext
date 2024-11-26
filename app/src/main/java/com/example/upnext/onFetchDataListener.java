package com.example.upnext;

import com.example.upnext.Models.NewsHeadlines;

import java.util.List;

public interface onFetchDataListener <NewsApiResponse> {
    void onFetchData(List<NewsHeadlines> list,String Message);
    void onError(String message);
}
