package com.koreatravel.tabitomo.domain.dto;

import java.time.LocalDateTime;

public interface StorybookListDTO {
    Integer getBooknum();
    String getTitle();
    String getSubtitle();
    String getThumbnail();
    Integer getLikes();
    LocalDateTime getCreateDate();
    String getNickname();
}
