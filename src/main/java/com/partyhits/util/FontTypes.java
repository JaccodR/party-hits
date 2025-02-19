package com.partyhits.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FontTypes
{
    REGULAR("RS Regular"),
    ARIAL("Arial"),
    TIMES_NEW_ROMAN("Times New Roman");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}
