__kernel void test(__global const long *o1, __global const long *o2,
                   __global long *result) {
  int current = get_global_id(0);

  result[current] = ((o1[current] * current) << 2) +
                    (o2[current] % 2) / (current + 1) +
                    (current >> 2) / ((current + 1) << 4);
}
