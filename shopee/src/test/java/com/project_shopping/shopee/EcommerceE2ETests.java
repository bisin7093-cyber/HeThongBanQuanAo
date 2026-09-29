package com.project_shopping.shopee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EcommerceE2ETests {
 @Autowired TestRestTemplate http;
 @Autowired ObjectMapper mapper;
 private final String adminLogin="{\"email\":\"admin@ecommerce.test\",\"password\":\"admin-test-password\"}";

 @Test void buyerChecksOutAdminConfirmsAndBuyerReadsNotification() throws Exception {
  String email="buyer-"+UUID.randomUUID()+"@test.local"; register(email);
  String userToken=login(email,"password"); String adminToken=login("admin@ecommerce.test","admin-test-password");
  JsonNode product=createProduct(adminToken,2,"Áo thun");
  assertThat(product.get("category").asText()).isEqualTo("Áo thun");
  createProduct(adminToken,0,"Áo thun");
  JsonNode allProducts=body(get("/api/products?page=0&pageSize=100",null),HttpStatus.OK); assertThat(allProducts.toString()).contains(product.get("name").asText()).contains("\"category\":\"Áo thun\"");
  assertThat(body(get("/api/products?keyword="+URLEncoder.encode(product.get("name").asText(),StandardCharsets.UTF_8),null),HttpStatus.OK).get("totalElements").asLong()).as("keyword filter").isEqualTo(1);
  assertThat(body(get("/api/products?category=Áo thun",null),HttpStatus.OK).get("totalElements").asLong()).as("category filter").isEqualTo(2);
  String searchQuery="/api/products?keyword="+URLEncoder.encode(product.get("name").asText(),StandardCharsets.UTF_8)+"&category=Áo thun&size=M&color=Black&minPrice=24&maxPrice=26&inStock=true&page=0&pageSize=5&sort=price_asc";
  JsonNode search=body(get(searchQuery,null),HttpStatus.OK); assertThat(search.get("items").size()).as(search.toString()).isEqualTo(1); assertThat(search.get("totalElements").asLong()).as(search.toString()).isEqualTo(1);
  assertThat(body(get("/api/products/categories",null),HttpStatus.OK).toString()).contains("Áo thun");
  assertThat(body(get("/api/products/filters",null),HttpStatus.OK).get("sizes").toString()).contains("M");
  assertThat(body(get("/api/products/"+product.get("id").asLong()+"/related?limit=4",null),HttpStatus.OK).size()).isEqualTo(1);
  JsonNode productDetail=body(get("/api/products/"+product.get("id").asLong(),userToken),HttpStatus.OK);
  long variantId=productDetail.get("variants").get(0).get("id").asLong();
  body(post("/api/cart/items",userToken,Map.of("variantId",variantId,"quantity",1)),HttpStatus.CREATED);
  JsonNode cart=body(post("/api/cart/items",userToken,Map.of("variantId",variantId,"quantity",1)),HttpStatus.CREATED);
  assertThat(cart.get("items").size()).isEqualTo(1); assertThat(cart.get("items").get(0).get("quantity").asInt()).isEqualTo(2);
  cart=body(put("/api/cart/items/"+cart.get("items").get(0).get("id").asLong(),userToken,Map.of("quantity",2)),HttpStatus.OK);
  assertThat(cart.get("totalAmount").decimalValue()).isEqualByComparingTo("50.00");
  JsonNode order=body(post("/api/orders",userToken,Map.of("shippingAddress","1 Example Street","phone","0900000000","paymentMethod","COD")),HttpStatus.CREATED);
  long orderId=order.get("id").asLong(); assertThat(order.get("status").asText()).isEqualTo("PENDING"); assertThat(order.get("payment").get("paymentStatus").asText()).isEqualTo("PENDING");
  JsonNode emptyCart=body(get("/api/cart",userToken),HttpStatus.OK); assertThat(emptyCart.get("items")).isEmpty();
  body(put("/api/admin/orders/"+orderId+"/confirm",adminToken),HttpStatus.OK);
  assertThat(body(put("/api/admin/orders/"+orderId+"/cancel",adminToken),HttpStatus.CONFLICT).get("status").asInt()).isEqualTo(409);
  JsonNode notifications=body(get("/api/notifications",userToken),HttpStatus.OK); assertThat(notifications.size()).isEqualTo(1); assertThat(notifications.get(0).get("orderId").asLong()).isEqualTo(orderId);
  body(put("/api/notifications/"+notifications.get(0).get("id").asLong()+"/read",userToken),HttpStatus.OK);
  assertThat(body(get("/api/orders/"+orderId,userToken),HttpStatus.OK).get("status").asText()).isEqualTo("CONFIRMED");
 }

 @Test void cancellationRestoresStockAndCannotBeConfirmedAfterward() throws Exception {
  String email="cancel-"+UUID.randomUUID()+"@test.local"; register(email);
  String userToken=login(email,"password"), adminToken=login("admin@ecommerce.test","admin-test-password");
  JsonNode product=createProduct(adminToken,5); long variantId=product.get("variants").get(0).get("id").asLong();
  body(post("/api/cart/items",userToken,Map.of("variantId",variantId,"quantity",3)),HttpStatus.CREATED);
  JsonNode order=body(post("/api/orders",userToken,Map.of("shippingAddress","2 Example Street","phone","0911111111","paymentMethod","BANKING")),HttpStatus.CREATED);
  assertThat(body(get("/api/products/"+product.get("id").asLong(),userToken),HttpStatus.OK).get("variants").get(0).get("stockQuantity").asInt()).isEqualTo(2);
  body(put("/api/admin/orders/"+order.get("id").asLong()+"/cancel",adminToken),HttpStatus.OK);
  assertThat(body(get("/api/products/"+product.get("id").asLong(),userToken),HttpStatus.OK).get("variants").get(0).get("stockQuantity").asInt()).isEqualTo(5);
  assertThat(body(put("/api/admin/orders/"+order.get("id").asLong()+"/confirm",adminToken),HttpStatus.CONFLICT).get("status").asInt()).isEqualTo(409);
  JsonNode cart=body(post("/api/cart/items",userToken,Map.of("variantId",variantId,"quantity",1)),HttpStatus.CREATED);
  assertThat(request("/api/cart/items/"+cart.get("items").get(0).get("id").asLong(),HttpMethod.DELETE,userToken,null).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  assertThat(body(get("/api/cart",userToken),HttpStatus.OK).get("items")).isEmpty();
 }

 @Test void rejectsBadLoginInsufficientStockEmptyCartAndCrossUserAccess() throws Exception {
  assertThat(post("/api/auth/login",null,Map.of("email","missing@test.local","password","wrong")).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  String buyer="negative-"+UUID.randomUUID()+"@test.local", other="other-"+UUID.randomUUID()+"@test.local";
  register(buyer); register(other); String token=login(buyer,"password"), otherToken=login(other,"password"), adminToken=login("admin@ecommerce.test","admin-test-password");
  assertThat(post("/api/auth/login",null,Map.of("email",buyer,"password","wrong")).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  assertThat(post("/api/auth/register",null,Map.of("fullName","Duplicate","email",buyer,"password","password")).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  assertThat(get("/api/admin/orders",token).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  assertThat(post("/api/orders",token,Map.of("shippingAddress","Address","phone","0900000000","paymentMethod","COD")).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  assertThat(get("/api/admin/orders/99999999",adminToken).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  assertThat(put("/api/admin/orders/99999999/confirm",adminToken).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  assertThat(post("/api/orders",token,Map.of("shippingAddress","Address","phone","0900000000","paymentMethod","UNKNOWN")).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  JsonNode inactive=createProduct(adminToken,3);
  body(put("/api/admin/products/"+inactive.get("id").asLong(),adminToken,Map.of("name","Inactive test","description","test","price",25.00,"status","INACTIVE")),HttpStatus.OK);
  assertThat(post("/api/cart/items",token,Map.of("variantId",inactive.get("variants").get(0).get("id").asLong(),"quantity",1)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  JsonNode product=createProduct(adminToken,1); long variant=product.get("variants").get(0).get("id").asLong();
  assertThat(post("/api/cart/items",token,Map.of("variantId",variant,"quantity",0)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  assertThat(post("/api/cart/items",token,Map.of("variantId",variant,"quantity",2)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  body(post("/api/cart/items",token,Map.of("variantId",variant,"quantity",1)),HttpStatus.CREATED);
  body(put("/api/admin/products/"+product.get("id").asLong()+"/variants/"+variant,adminToken,Map.of("size","M","color","Black","stockQuantity",0,"price",25.00)),HttpStatus.OK);
  assertThat(post("/api/orders",token,Map.of("shippingAddress","Address","phone","0900000000","paymentMethod","COD")).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  assertThat(body(get("/api/orders",token),HttpStatus.OK)).isEmpty();
  body(put("/api/admin/products/"+product.get("id").asLong()+"/variants/"+variant,adminToken,Map.of("size","M","color","Black","stockQuantity",1,"price",25.00)),HttpStatus.OK);
  JsonNode order=body(post("/api/orders",token,Map.of("shippingAddress","Address","phone","0900000000","paymentMethod","COD")),HttpStatus.CREATED);
  assertThat(get("/api/orders/"+order.get("id").asLong(),otherToken).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  body(put("/api/admin/orders/"+order.get("id").asLong()+"/confirm",adminToken),HttpStatus.OK);
  JsonNode notification=body(get("/api/notifications",token),HttpStatus.OK).get(0);
  assertThat(put("/api/notifications/"+notification.get("id").asLong()+"/read",otherToken).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
 }

 private void register(String email) throws Exception { body(post("/api/auth/register",null,Map.of("fullName","Test Buyer","email",email,"password","password","phone","0900000000")),HttpStatus.CREATED); }
 private String login(String email,String password) throws Exception { return body(post("/api/auth/login",null,Map.of("email",email,"password",password)),HttpStatus.OK).get("accessToken").asText(); }
 private JsonNode createProduct(String token,int stock) throws Exception { return createProduct(token,stock,"Thời trang"); }
 private JsonNode createProduct(String token,int stock,String category) throws Exception {
  JsonNode p=body(post("/api/admin/products",token,Map.of("name","Test item "+UUID.randomUUID(),"category",category,"description","test","price",new BigDecimal("25.00"),"status","ACTIVE")),HttpStatus.CREATED);
  JsonNode v=body(post("/api/admin/products/"+p.get("id").asLong()+"/variants",token,Map.of("size","M","color","Black","stockQuantity",stock,"price",new BigDecimal("25.00"))),HttpStatus.CREATED);
  return mapper.createObjectNode().put("id",p.get("id").asLong()).put("name",p.get("name").asText()).put("category",p.get("category").asText()).set("variants",mapper.createArrayNode().add(v));
 }
 private ResponseEntity<String> get(String path,String token) { return request(path,HttpMethod.GET,token,null); }
 private ResponseEntity<String> post(String path,String token,Object body) { return request(path,HttpMethod.POST,token,body); }
 private ResponseEntity<String> put(String path,String token) { return request(path,HttpMethod.PUT,token,Map.of()); }
 private ResponseEntity<String> put(String path,String token,Object body) { return request(path,HttpMethod.PUT,token,body); }
 private ResponseEntity<String> request(String path,HttpMethod method,String token,Object body) {
  HttpHeaders headers=new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON); if(token!=null) headers.setBearerAuth(token);
  return http.exchange(path,method,new HttpEntity<>(body,headers),String.class);
 }
 private JsonNode body(ResponseEntity<String> response,HttpStatus expected) throws Exception { assertThat(response.getStatusCode()).as(response.getBody()).isEqualTo(expected); return mapper.readTree(response.getBody()==null?"{}":response.getBody()); }
}
