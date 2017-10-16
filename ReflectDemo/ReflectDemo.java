package com.zhao.Demo.Reflect;

import java.lang.reflect.Field;

public class ReflectDemo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Class<?> c = Heros.class;
		try {
//			Class c = Class.forName("Heros");		
			//用反射机制创建一个对象的实例 相当于new 但是与new不同的地方是该方法不需要知道所声明的对象的类型或者构造方法
			Object object = c.newInstance();		
			//返回一个Field类型的对象，该对象包括由反射生成的对象c的类声明的属性和方法
			Field[] fields = c.getDeclaredFields();
			System.out.println("Heros所有属性： ");
			for(Field f : fields){
				System.out.println(f);
			}
			
			Field field = c.getDeclaredField("name");
			
			field.setAccessible(true);
			field.set(object, "炸弹人");
			System.out.println("修改后的属性值");
			System.out.println(field.get(object));
			System.out.println("修改属性后的Heros:");
			System.out.println((Heros)object);
		} catch (Exception e) {
	
		}
	}
	}


