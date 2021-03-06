package com.mirror.ormtransaction.common;

import com.mirror.ormtransaction.annotation.MId;
import com.mirror.ormtransaction.utils.MStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * @author: mirror_huang
 * @qq: 1755496180
 * @description: 拼接增删改查的sql, 从Object转化为数据库中的Field
 * @create: 2020-06-24 00:30
 **/
@Component
public class BuildParamToFieldManager<T> {

    /**
     * 获取本类中的实体对象
     * TODO:暂不理解
     *
     * @param clazz
     * @return
     */
    public Class<T> getClassInfo(Class<? extends BaseMapper> clazz) {
        //获取本类
        //返回表示此class所表示的实体（类，接口，基本类型或void）的直接超类的Type （泛型类的参数化）
        //比如说 StudentMapper extends BaseMapper<Student>
        //得到 BaseMapper<Student>的类型
        Type genericSuperClass = clazz.getGenericSuperclass();
        //判断获取超类中的类型是不是参数化类型
        if (genericSuperClass instanceof ParameterizedType) {
            //如果是参数化类型，则强制转化为参数化类型
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperClass;
            //获取泛型中的实际类型，可能会存在多个泛型，例如Map<K,V>,所以会返回Type[]数组
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            //我们做的都是单表映射，并且必须有一个泛型,所以我们取第一个泛型就可以获取到具体泛型的类型
            Type actualTypeArgument = actualTypeArguments[0];
            //并且把这个具体的泛型强制转化为我们需要的带泛型的类（因为泛型的类型本身就是个类，我们将强制转化）
            Class<T> returnType = (Class<T>) actualTypeArgument;
            return returnType;
        }
        return null;
    }

    /**
     * 拼接删除的sql
     *
     * @param clazz
     * @param sql
     * @param paramsList
     * @param id
     */
    public void buildDeleteFieldParams(Class<?> clazz, StringBuffer sql, List<Object> paramsList, Object id) {
        //获取表名
        String tableName = MStringUtils.getTableName(clazz);
        //申明主键名称
        String idName = "";
        //获取实体类中的属性
        Field[] fields = clazz.getDeclaredFields();
        //循环查询出实体类中主键对应的数据库字段名称
        for (Field field : fields) {
            //因为属性有可能是private不能访问的,可以设置强行访问它
            field.setAccessible(true);
            if (Objects.equals("", idName)) {
                idName = MStringUtils.getTableIdName(field);
            } else {
                break;
            }
        }
        if (Objects.equals("", idName)) {
            throw new RuntimeException("实体类" + clazz.getSimpleName() + "中没有找到主键注解!");
        }
        //将找到的主键名称拼接到删除的sql里面
        sql.append("delete from ").append(tableName).append(" where ").append(idName).append(" =? ");
        paramsList.add(id);
    }

    /**
     * 拼接查询的语句
     *
     * @param entity
     * @param sql
     * @param primaryId
     * @param paramList
     */
    public void buildSelectFieldParams(T entity, StringBuffer sql, Object primaryId, List<Object> paramList) {
        //首先根据clazz获取表名称
        String tableName = MStringUtils.getTableName(entity.getClass());
        //获取ID的名称
        String idName = "";
        sql.append("select ");
        Field[] fields = entity.getClass().getDeclaredFields();
        if (null == fields) {
            throw new RuntimeException("获取属性值为空!");
        }
        for (Field field : fields) {
            //因为属性有可能是private不能访问的，设置为可以强制访问的
            field.setAccessible(true);
            //拼接主键名称
            if ("".equals(idName)) {
                idName = MStringUtils.getTableFieldName(field);
                sql.append(idName).append(",");
            }
            //拼接其他字段的名称
            String tableFieldName = MStringUtils.getTableFieldName(field);
            if (!"".equals(tableFieldName)) {
                sql.append(tableFieldName).append(",");
            }
        }
        //去掉最后一个逗号
        sql.deleteCharAt(sql.length() - 1).append(" from ").append(tableName);
        if (null != primaryId) {
            sql.append(" where ").append(idName).append(" =? ");
            paramList.add(primaryId);
        }
    }

    /**
     * 参数新增拼装
     *
     * @param entity
     * @param sql
     * @param paramsList
     */
    public void buildInsertFieldParams(T entity, StringBuffer sql, List<Object> paramsList) {
        //首先根据clazz获取表名称
        final String tableName = MStringUtils.getTableName(entity.getClass());
        //其次开始拼接sql
        sql.append("insert into ").append(tableName).append(" ( ");
        try {
            //获取字段名称
            Field[] fields = entity.getClass().getDeclaredFields();
            if (!Objects.equals("", fields)) {
                for (Field field : fields) {
                    //根据属性值获取数据库字段名称
                    String tableFieldName = MStringUtils.getTableFieldName(field);
                    //因为属性有可能是private不能访问的，那么我们就通过强吻她
                    field.setAccessible(true);
                    //字段取到之后，开始将值填充参数列表中
                    Object o = field.get(entity);
                    if (o != null && !"".equals(tableFieldName)) {
                        //继续拼接sql
                        sql.append(tableFieldName).append(",");
                        paramsList.add(o);
                    }
                }
                //去掉字段中最后一个逗号,
                sql.deleteCharAt(sql.length() - 1).append(" ) values ( ");
                for (Object o : paramsList) {
                    //sql.append( o ).append( "," );
                    sql.append(" ? ,");
                }
                //去掉占位符中的最后一个逗号
                sql.deleteCharAt(sql.length() - 1).append(" ) ");

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拼接update操作的sql
     *
     * @param entity
     * @param sql
     * @param paramsList
     */
    public void buildUpdateFieldParams(T entity, StringBuffer sql, List<Object> paramsList) {
        //首先根据clazz获取表名称
        String tableName = MStringUtils.getTableName(entity.getClass());
        //其次开始拼接sql
        sql.append("update ").append(tableName).append(" set ");
        try {
            //获取字段名称
            Field[] fields = entity.getClass().getDeclaredFields();
            if (!Objects.equals("", fields)) {
                String idName = "";
                Object idValues = null;
                for (Field field : fields) {
                    //根据属性值获取数据库字段名称
                    String tableFieldName = MStringUtils.getTableFieldName(field);
                    //因为属性有可能是private不能访问的，那么我们就通过强吻她
                    field.setAccessible(true);
                    if (StringUtils.isEmpty(idName)) {
                        idName = MStringUtils.getTableIdName(field);
                    } else {
                        //字段取到之后，开始将值填充参数列表中
                        Object o = field.get(entity);
                        if (o != null) {
                            //继续拼接sql
                            sql.append(tableFieldName).append(" = ?,");
                            paramsList.add(o);
                        }
                    }
                    if (field.isAnnotationPresent(MId.class)) {
                        idValues = field.get(entity);
                    }

                }
                //去掉字段中最后一个逗号,
                sql.deleteCharAt(sql.length() - 1).append(" where " + idName + " = ? ");
                paramsList.add(idValues);

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
