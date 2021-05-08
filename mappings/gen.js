#!/usr/bin/env node
const fs = require('fs')
const assert = require('assert').strict

const spigot2yarnLines = fs.readFileSync('spigot2yarn.srg', 'utf8').split("\n").map(line=>line.split(" "))
const three2two = line=>{
  assert(line.length==3)
  return [line[1],line[2]]
}
const CL = spigot2yarnLines.filter(line=>line[0]=='CL:').map(three2two)
const FD = spigot2yarnLines.filter(line=>line[0]=='FD:').map(three2two)
const MD = spigot2yarnLines.filter(line=>line[0]=='MD:').map(line=>{
  assert(line.length==5)
  return [line[1],line[2],line[3],line[4]]
})

const nCL = CL.map(x=>[x[0],x[0]])
const nCL1 = nCL
const nFD = FD.map(x=>{
  const cl = x[0].split("/").slice(0,-1).join("/")
  const fd = x[0].split("/").pop()
  let yarnfd = x[1].split("/").pop()
  if(fd != yarnfd){
    yarnfd += "_"
  }
  if(yarnfd.startsWith("field_") && fd.length>3){
    yarnfd = fd
  }
  return [`${cl}/${fd}`, `${cl}/${yarnfd}`]
})
const nFD1 = nFD.map(x=>[x[1],x[0]])
const ignoreMDList = new Set()
const nMD = MD.map(x=>{
  const cl = x[0].split("/").slice(0,-1).join("/")
  const md = x[0].split("/").pop()
  const sig = x[1]
  let yarnmd = x[2].split("/").pop()
  if(md.startsWith("method_")){
    yarnmd=md
  }
  if(md!=yarnmd){
    yarnmd+="_"
  }
  if(['PotionUtil','GeneratorAccess','ProtoChunk','IChunkProvider','IAsyncTaskHandler'].includes(cl.split("/").pop()) || // mercury can't handle it
    yarnmd==md+"_" || // net/minecraft/server/Entity/damageEntity // map broken
    md=='damageEntity' || // net/minecraft/server/Entity/damageEntity // map broken
    md=='isOutsideWorld' || // net/minecraft/server/World/isOutsideWorld // mercury can't handle it
    yarnmd=='syncWorldEvent_' || // net/minecraft/server/GeneratorAccess/syncWorldEvent_  // mercury can't handle it
    yarnmd=='getPlayerManager_' || // net/minecraft/server/DedicatedServer/getPlayerManager_  // mercury can't handle it
    yarnmd=='removePendingUpdate_' || // net/minecraft/server/LightEngineGraph/removePendingUpdate_ // mercury can't handle it
    md=='getChunkProvider' || // net/minecraft/server/level/WorldServer/getChunkProvider // mercury can't handle it
    (md=='hasCustomName' && yarnmd=='S_') // net/minecraft/server/Entity/hasCustomName // map broken
  ){
    yarnmd=md
    if(md.length>3){
      ignoreMDList.add(md)
    }
  }
  return [`${cl}/${md}`, sig, `${cl}/${yarnmd}`, sig]
})
nMD.forEach(x=>{
  const cl = x[0].split("/").slice(0,-1).join("/")
  const md = x[0].split("/").pop()
  if(ignoreMDList.has(md)) {
    x[2] = `${cl}/${md}`
  }
})
const nMD1 = nMD.map(x=>[x[2],x[1],x[0],x[3]])

const result = nCL.map(x=>`CL: ${x.join(" ")}`).concat(nFD.map(x=>`FD: ${x.join(" ")}`)).concat(nMD.map(x=>`MD: ${x.join(" ")}`)).join("\n")
const result1 = nCL1.map(x=>`CL: ${x.join(" ")}`).concat(nFD1.map(x=>`FD: ${x.join(" ")}`)).concat(nMD1.map(x=>`MD: ${x.join(" ")}`)).join("\n")
fs.writeFileSync("map.srg", result)
fs.writeFileSync("unmap.srg", result1)
