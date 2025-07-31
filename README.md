# 💧 Water Drop Art

A mesmerizing Android app that animates falling water droplets which dynamically form any uploaded image using fluid, mesmerizing particle effects.

![Water Drop Art Demo](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)

## 🎨 Features

### 💧 Core Animation
- **Continuous Water Drop Animation**: Realistic droplet motion with gravity, fade, and bounce
- **Configurable Parameters**: Droplet size, speed, spawn rate, and random drift
- **Rain-like Effect**: Drops fall continuously from top to bottom across the entire screen
- **White Droplets**: Clean, elegant white water drops for visual appeal

### 🖼️ Image-to-WaterDrop Morphing
- **Dynamic Shape Formation**: Convert any image into droplet formation
- **Intelligent Interpolation**: Droplets flow smoothly to form the actual image shape
- **Fluid Motion**: Smooth water-like collection rather than rigid jumps
- **Shape Preservation**: Maintains the actual silhouette of uploaded images
- **Centered Display**: Images are centered and scaled appropriately (200x200)

### 🎭 Multiple Morphing Styles
- **Smooth**: Traditional fluid morphing
- **Pixelated**: Blocky, retro-style morphing
- **Edge Detection**: Sharp, defined edge morphing
- **Artistic**: Creative, stylized morphing

### 🎨 Image Filters
- **None**: Original image colors
- **Grayscale**: Monochrome effect
- **Sepia**: Vintage brown tone
- **Neon**: Vibrant, glowing colors

### ✨ Special Effects
- **Fireworks**: Explosive particle effects with colorful bursts
- **Galaxy**: Cosmic particle effects with star-like animations
- **Underwater**: Bubble effects with floating particles
- **Storm**: Lightning and thunder effects

### 📱 Advanced Features
- **Reverse Morph**: Transform morphed image back to rain with explosion effects
- **Video Recording**: Capture morphing animations as MP4 videos
- **GIF Creation**: Generate animated GIFs of the morphing process
- **Social Sharing**: Share videos and GIFs directly to social media platforms

### 🎮 Interactive Controls
- **Horizontally Scrollable Buttons**: All controls accessible on any screen size
- **Real-time Style Switching**: Change morphing styles on the fly
- **Filter Cycling**: Apply different filters instantly
- **Effect Toggle**: Switch between special effects seamlessly
- **Image Selection**: Easy gallery/camera integration

## 🚀 Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (API level 24)
- Kotlin 1.9.22
- Gradle 8.0+

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/WaterDropArt.git
   cd WaterDropArt
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the WaterDropArt folder

3. **Sync and Build**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on Device**
   ```bash
   ./gradlew installDebug
   ```

### Required Permissions
The app requires the following permissions:
- `READ_EXTERNAL_STORAGE`: Access images from gallery
- `READ_MEDIA_IMAGES`: Modern image access (Android 13+)
- `CAMERA`: Take photos directly
- `WRITE_EXTERNAL_STORAGE`: Save videos and GIFs
- `MANAGE_EXTERNAL_STORAGE`: File management for sharing

## 🎯 Usage Guide

### Basic Operation

1. **Launch the App**
   - The app starts with continuous rain animation
   - White water drops fall from top to bottom

2. **Upload an Image**
   - Tap the **"+" button** to select an image
   - Choose from gallery or camera
   - The app will process the image in the background

3. **Watch the Morphing**
   - Animation continues for 2 seconds
   - Drops then morph into your image shape
   - Image displays for 8 seconds
   - Returns to continuous rain animation

### Advanced Controls

#### **Style Button** 🔄
- Cycles through morphing styles: Smooth → Pixelated → Edge Detection → Artistic
- Each style creates a different visual effect

#### **Filter Button** 🎨
- Cycles through image filters: None → Grayscale → Sepia → Neon
- Filters are applied to the morphed image

#### **Effect Button** ✨
- Cycles through special effects: None → Fireworks → Galaxy → Underwater → Storm
- Each effect adds unique particle animations

#### **Reverse Morph Button** 🔄
- Transforms current morphed image back to rain
- Creates explosion effects during transition
- Returns to continuous falling animation

#### **Record Video Button** 📹
- Records 5-second video of current animation
- Automatically shares to other apps
- Saves as MP4 file

#### **Create GIF Button** 🎬
- Creates 3-second animated GIF
- Automatically shares to other apps
- Saves as GIF file

### Performance Tips

- **Image Size**: Use images under 2MB for best performance
- **Complex Shapes**: Simple shapes morph more smoothly
- **Background Processing**: Wait for processing indicator to complete
- **Memory Management**: Close other apps for optimal performance

## 🧪 Testing Scenarios

### Test 1: Basic Image Morphing
1. Upload a simple shape (heart, star, circle)
2. **Expected**: Smooth morphing to exact shape
3. **Expected**: 8-second display time
4. **Expected**: Return to rain animation

### Test 2: Multiple Image Selections
1. Upload image A → morph → reverse
2. Upload image B → morph → reverse
3. Upload image C → morph
4. **Expected**: Clean transitions between images
5. **Expected**: No performance lag

### Test 3: Style and Filter Testing
1. Upload an image
2. Cycle through all styles and filters
3. **Expected**: Different visual effects for each combination
4. **Expected**: Smooth style transitions

### Test 4: Special Effects
1. Upload an image and let it morph
2. Switch between special effects
3. **Expected**: Unique particle animations for each effect
4. **Expected**: Performance remains smooth

### Test 5: Recording Features
1. Start a morphing sequence
2. Press "Record Video" or "Create GIF"
3. **Expected**: File saved and sharing dialog appears
4. **Expected**: Share to Instagram, Twitter, WhatsApp, etc.

## 🏗️ Architecture

### MVVM Pattern
- **Model**: `WaterDrop`, `AnimationState` data classes
- **View**: `MainScreen`, `WaterDropView` UI components
- **ViewModel**: `MainViewModel` business logic

### Key Components

#### **WaterDropView**
- Custom Android View for animation rendering
- Handles all drawing and physics calculations
- Manages particle systems and special effects

#### **MainViewModel**
- Coordinates animation state
- Manages image processing and morphing
- Handles user interactions

#### **Services**
- `MediaRecorderService`: Video and GIF creation
- `ShareService`: Social media sharing
- `VolumeButtonService`: Input handling

### Performance Optimizations
- **Reduced Particle Counts**: Limited drops, explosions, ripples
- **Background Processing**: Heavy operations on IO dispatcher
- **Memory Management**: Aggressive cleanup of old particles
- **Frame Rate Control**: 60 FPS with adaptive timing

## 🎨 Customization

### Adding New Styles
1. Add new enum value to `MorphingStyle`
2. Implement processing logic in `applyMorphing()`
3. Update UI to cycle through new styles

### Adding New Filters
1. Add new enum value to `FilterType`
2. Implement filter logic in `applyFilter()`
3. Update UI to cycle through new filters

### Adding New Effects
1. Add new enum value to `SpecialEffect`
2. Implement particle system in `updateDrops()`
3. Add drawing logic in `onDraw()`

## 🐛 Troubleshooting

### Common Issues

#### **App is Slow/Laggy**
- Reduce image complexity
- Close other background apps
- Restart the app

#### **Images Not Morphing**
- Check image format (JPEG, PNG supported)
- Ensure image has visible content
- Try simpler images

#### **Recording Not Working**
- Check storage permissions
- Ensure sufficient storage space
- Restart app and try again

#### **Sharing Not Working**
- Check app permissions
- Ensure target app is installed
- Try different sharing method

### Debug Logging
The app includes comprehensive debug logging:
- `"DEBUG: applyMorphing called, clearing old drops"`
- `"DEBUG: Found X old morphing drops to explode"`
- `"DEBUG: Created X new morphing drops"`

## 📱 System Requirements

- **Android Version**: 7.0+ (API 24)
- **RAM**: 2GB minimum, 4GB recommended
- **Storage**: 100MB free space
- **Screen**: Any resolution supported
- **Camera**: Optional (for direct photo capture)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup
```bash
# Clone repository
git clone https://github.com/yourusername/WaterDropArt.git

# Open in Android Studio
# Sync Gradle files
# Run on device or emulator
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Jetpack Compose**: Modern Android UI toolkit
- **Kotlin Coroutines**: Asynchronous programming
- **Material Design**: UI/UX guidelines
- **Android Canvas**: Custom drawing capabilities

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/WaterDropArt/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/WaterDropArt/discussions)
- **Email**: support@waterdropart.com

---

**Made with ❤️ and 💧 by the Water Drop Art Team**

*Transform your images into mesmerizing water drop animations!* 