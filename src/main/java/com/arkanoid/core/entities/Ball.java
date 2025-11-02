    package com.arkanoid.core.entities;

    import com.arkanoid.systems.sound.SoundManager;
    import javafx.scene.canvas.GraphicsContext;
    import javafx.scene.paint.Color;
    import javafx.scene.image.Image;

    import java.io.InputStream;
    import java.util.HashMap;
    import java.util.Map;

    public class Ball extends MovableObject {
        private double radius;
        private Color color;
        private double minX, minY, maxX, maxY;
        private boolean attachedToPaddle = false;
        private Image ballImage;
        private static final Map<String, Image> SKINS = new HashMap<>();
        private static String currentSkin = "Default";

        static {
            loadSkins();
        }
        public Ball(double x, double y, double radius, double speed) {
            super(x - radius, y - radius, radius * 2, radius * 2, speed);
            this.radius = radius;
            this.color = Color.RED;
    //        this.velocityX = speed * 0.7;
    //        this.velocityY = -speed;
            this.velocityX = 0;
            this.velocityY = 0;
            attachedToPaddle = true;
    //        loadDefaultImage();
            equipSkin(currentSkin);
        }



        public void setBounds(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }


        @Override
        public void update(double deltaTime) {
            move(deltaTime);
            checkWallCollision();
        }

        public void launch() {
            if (attachedToPaddle) {
                attachedToPaddle = false;
                velocityY = -Math.abs(speed); // bay lên trên
                velocityX = 0;                // bay thẳng, hoặc bạn có thể random nhẹ
            }
        }

        private void checkWallCollision() {
            if (x <= minX) {
                x = minX;
                reverseX();
            }
            if (x + width >= maxX) {
                x = maxX - width;
                reverseX();
            }
            if (y <= minY) {
                y = minY;
                reverseY();
            }
        }

        public void checkPaddleCollision(Paddle paddle) {
            if (intersects(paddle) && velocityY > 0) {
                SoundManager.playSound("paddleBounce.wav");
                y = paddle.getY() - height;

                double hitPosition = (getCenterX() - paddle.getCenterX()) / (paddle.getWidth() / 2);
                velocityX = hitPosition * speed * 0.8;
                velocityY = -Math.abs(velocityY);

                double totalSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                if (totalSpeed != speed) {
                    double ratio = speed / totalSpeed;
                    velocityX *= ratio;
                    velocityY *= ratio;
                }
            }
        }

        private static void loadSkins() {
            String[] skinNames = { "Fire", "Ice", "Rainbow", "Default"};
            for (String name : skinNames) {
                try (InputStream stream = Ball.class.getResourceAsStream("/images/" + name + ".png")) {
                    if (stream != null) {
                        SKINS.put(name, new Image(stream));
                    } else {
                        System.err.println("Không tìm thấy ảnh: " + name + ".png");
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi load skin " + name + ": " + e.getMessage());
                }
            }

            // Nếu Default không load được, tạo 1 hình tròn mặc định
            if (!SKINS.containsKey("Default")) {
                SKINS.put("Default", null);
            }
        }
    //    private void loadDefaultImage() {
    //        try (InputStream stream = getClass().getResourceAsStream("/images/ball.png")) {
    //            if (stream != null) {
    //                this.ballImage = new Image(stream);
    //            } else {
    //                System.err.println("Cảnh báo: Không tìm thấy file ảnh ball.png trong /images/. Dùng màu mặc định.");
    //            }
    //        } catch (Exception e) {
    //            System.err.println("Lỗi khi tải ảnh Ball: " + e.getMessage());
    //        }
    //    }

        public static Image getSkin(String skinName) {
            return SKINS.getOrDefault(skinName, SKINS.get("Default"));
        }
        public void equipSkin(String skinName) {
            Image skin = SKINS.getOrDefault(skinName, SKINS.get("Default"));
            this.ballImage = skin;
        }
        @Override
        public void render(GraphicsContext gc) {
            if (ballImage != null) {
                gc.drawImage(ballImage, x, y, width, height);
            } else {
                gc.setFill(color);
                gc.fillOval(x, y, width, height);
            }
        }
        public void setBallImage(Image image) {
            this.ballImage = image;
        }

        public boolean isOutOfBounds() {
            return y > maxY;
        }

        public void reset(double x, double y) {
            this.x = x - radius;
            this.y = y - radius;
            this.velocityX = speed * 0.7;
            this.velocityY = -speed;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public double getRadius() {
            return radius;
        }

        public boolean isAttachedToPaddle() {
            return attachedToPaddle;
        }

        public void setAttachedToPaddle(boolean attachedToPaddle) {
            this.attachedToPaddle = attachedToPaddle;
        }
        public static String getCurrentSkin() {
            return currentSkin;
        }

        public static void setCurrentSkin(String skinName) {
            if (SKINS.containsKey(skinName)) {
                currentSkin = skinName;
                System.out.println("🎨 Skin bóng hiện tại: " + skinName);
            } else {
                System.err.println("⚠️ Skin không tồn tại: " + skinName);
            }
        }
    }
