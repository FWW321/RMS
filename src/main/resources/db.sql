# 用户凭证表
CREATE TABLE user_credential (
                                 user_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
                                 username VARCHAR(255) NOT NULL COMMENT '用户名',
                                 password VARCHAR(255) NOT NULL COMMENT '密码',
                                 deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
                                 version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '版本号',
                                 FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) COMMENT '用户凭证表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# 用户表
CREATE TABLE user (
                      user_id INT UNSIGNED PRIMARY KEY COMMENT '用户ID',
                      role ENUM('收银员', '厨师', '服务员', '系统管理员','顾客','客服') NOT NULL COMMENT '角色',
                      phone VARCHAR(20) NOT NULL COMMENT '手机号',
                      email VARCHAR(255) COMMENT '邮箱',
                      register_time DATETIME NOT NULL COMMENT '注册时间',
                      deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
                      version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '版本号'
) COMMENT '用户表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# 餐饮类别表
CREATE TABLE category (
                          category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '类别ID',
                          name VARCHAR(255) NOT NULL COMMENT '类别名称',
                          deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
                          version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '版本号'
) COMMENT '餐饮类别表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# 菜品表
CREATE TABLE dish (
                      dish_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '菜品ID',
                      name VARCHAR(255) NOT NULL COMMENT '菜品名称',
                      description TEXT COMMENT '菜品描述',
                      price DECIMAL(10,2) NOT NULL COMMENT '菜品价格',
                      category_id INT COMMENT '类别ID',
                      image VARCHAR(255) COMMENT '图片地址',
                      deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
                      version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '版本号',
                      FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE SET NULL
) COMMENT '菜品表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# 订单表
CREATE TABLE order_table (
                         order_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
                         user_id INT NOT NULL COMMENT '用户ID',
                         status VARCHAR(50) NOT NULL COMMENT '订单状态',
                         total_price DECIMAL(10,2) NOT NULL COMMENT '总价',
                         create_time DATETIME NOT NULL COMMENT '创建时间',
                         deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
                         version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '版本号',
                         FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) COMMENT '订单表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# 订单详情表
CREATE TABLE order_detail (
                              order_detail_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单详情ID',
                              order_id INT NOT NULL COMMENT '订单ID',
                              dish_id INT NOT NULL COMMENT '菜品ID',
                              quantity INT NOT NULL COMMENT '数量',
                              subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
                              deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
                              version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '版本号',
                              FOREIGN KEY (order_id) REFERENCES order_table (order_id) ON DELETE CASCADE,
                              FOREIGN KEY (dish_id) REFERENCES dish(dish_id) ON DELETE CASCADE
) COMMENT '订单详情表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE express_info (
                              express_id INT PRIMARY KEY COMMENT '快递ID',
                              order_id INT NOT NULL COMMENT '订单ID',
                              express_company VARCHAR(50) NOT NULL COMMENT '快递公司',
                              express_number VARCHAR(50) NOT NULL COMMENT '快递单号',
                              delivery_time DATETIME NOT NULL COMMENT '发货时间',
                              FOREIGN KEY (order_id) REFERENCES order_table (order_id)
) COMMENT '快递信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 员工表
CREATE TABLE employee (
                          employee_id INT PRIMARY KEY COMMENT '员工ID',
                          username VARCHAR(50) NOT NULL COMMENT '用户名',
                          password VARCHAR(50) NOT NULL COMMENT '密码',
                          role ENUM('收银员', '厨师', '服务员', '系统管理员') NOT NULL COMMENT '角色',
                          is_active TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否启用，1表示启用，0表示停用'
) COMMENT '员工表';

-- 员工订单关联表
CREATE TABLE employee_order (
                                employee_id INT NOT NULL COMMENT '员工ID',
                                order_id INT NOT NULL COMMENT '订单ID',
                                PRIMARY KEY (employee_id, order_id),
                                FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
                                FOREIGN KEY (order_id) REFERENCES order_table (order_id)
) COMMENT '员工订单关联表';


# 在user_credential表上创建触发器，当逻辑删除用户凭证时，同时逻辑删除对应的用户信息
DELIMITER //
CREATE TRIGGER tr_user_credential_delete
    BEFORE UPDATE ON user_credential
    FOR EACH ROW
BEGIN
    IF OLD.deleted = 0 AND NEW.deleted = 1 THEN
        UPDATE user SET deleted = 1 WHERE user_id = OLD.user_id;
    END IF;
END;
//
DELIMITER ;

# 在category表上创建触发器，当逻辑删除类别时，同时逻辑删除对应的菜品信息
DELIMITER //
CREATE TRIGGER tr_category_delete
    BEFORE UPDATE ON category
    FOR EACH ROW
BEGIN
    IF OLD.deleted = 0 AND NEW.deleted = 1 THEN
        UPDATE dish SET deleted = 1 WHERE category_id = OLD.category_id;
    END IF;
END;
//
DELIMITER ;

# 在order_table表上创建触发器，当逻辑删除订单时，同时逻辑删除对应的订单详情信息
DELIMITER //
CREATE TRIGGER tr_order_table_delete
    BEFORE UPDATE ON order_table
    FOR EACH ROW
BEGIN
    IF OLD.deleted = 0 AND NEW.deleted = 1 THEN
        UPDATE order_detail SET deleted = 1 WHERE order_id = OLD.order_id;
    END IF;
END;
//
DELIMITER ;

