package com.koreatravel.tabitomo.service.storybook;

import com.koreatravel.tabitomo.domain.dto.storybook.SaveRequestDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;

import java.util.List;

public interface StorybookService {
    // Add method signatures that match the EditorService methods being used
    StorybookDTO getstory(Integer booknum);
    Integer tempsave(SaveRequestDTO saveRequestDTO);
    List<StorybookListDTO> getStorybookList();
    void delete(Integer booknum);
    Object gettemp(Integer tempId);
    Integer saveTempAsPost(SaveRequestDTO saveRequestDTO);
    Integer savePost(SaveRequestDTO saveRequestDTO);
    void tempdel(Integer tempId);
}
