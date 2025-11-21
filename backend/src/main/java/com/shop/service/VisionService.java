package com.shop.service;

public interface  VisionService {

    String analyzeImageUrl(String imageUrl, String question);

    String analyzeImageBase64(String imageBase64, String question);
}