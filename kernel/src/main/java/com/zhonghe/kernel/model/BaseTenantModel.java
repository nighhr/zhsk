package com.zhonghe.kernel.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class BaseTenantModel implements Serializable {

    private static final long serialVersionUID = -7155211725788483690L;

    protected Long tenantId;

    private String id;
}

