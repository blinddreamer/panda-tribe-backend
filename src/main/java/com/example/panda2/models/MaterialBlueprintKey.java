package com.example.panda2.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class MaterialBlueprintKey implements Serializable {

    @Column(name = "\"typeID\"")
    private Integer typeId;

    @Column(name = "\"materialTypeID\"")
    private Integer materialTypeId;
}