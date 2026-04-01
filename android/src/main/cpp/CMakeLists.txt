cmake_minimum_required(VERSION 3.10.2)

project(securityshield)

set(CMAKE_ANDROID_STL_TYPE c++_static)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

add_library(
    securityshield
    SHARED
    anti_frida.cpp
    native_integrity.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    securityshield
    ${log-lib}
)