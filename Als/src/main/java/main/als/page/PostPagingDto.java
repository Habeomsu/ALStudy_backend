package main.als.page;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PostPagingDto {


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagingDto{

        private int page;
        private int size;
        private String sort;

    }


}
