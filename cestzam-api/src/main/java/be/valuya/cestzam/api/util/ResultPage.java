package be.valuya.cestzam.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultPage<T> {
    private int totalCount;
    private List<T> pageItems;
}
