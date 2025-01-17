package main.als.page;

public class PagingConverter {

    public static PostPagingDto.PagingDto toPagingDto(int page,int size,String sort){
        return PostPagingDto.PagingDto.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();
    }
}
